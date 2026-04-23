package com.mungcle.gateway.infrastructure.resilience

import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadFullException
import io.github.resilience4j.bulkhead.BulkheadRegistry
import io.github.resilience4j.kotlin.bulkhead.executeSuspendFunction
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException
import java.util.concurrent.TimeUnit

/**
 * gRPC 호출을 Resilience4j Bulkhead → TimeLimiter → Retry → CircuitBreaker 순으로 감싸는 유틸리티.
 *
 * 적용 순서 (안쪽 → 바깥쪽):
 *   CircuitBreaker (가장 안쪽) → Retry (지수 백오프) → TimeLimiter (5s 타임아웃) → Bulkhead (가장 바깥쪽)
 *
 * - Bulkhead: 서비스별 동시 호출 수 제한 — 한 서비스의 과부하가 전체로 전파되지 않도록 격리
 * - TimeLimiter: 5초 초과 시 TimeoutException 발생 (kotlinx.coroutines.withTimeout 사용)
 * - Retry: 인프라 장애(UNAVAILABLE 등)에 대해 지수 백오프로 최대 3회 재시도.
 *           CB OPEN 시 발생하는 ServiceUnavailableException은 재시도하지 않는다.
 * - CircuitBreaker: 누적 실패율이 임계값 초과 시 OPEN → 즉시 거부
 *
 * 비즈니스 에러(NOT_FOUND, ALREADY_EXISTS 등)는 재시도 및 CB 실패로 기록하지 않는다.
 */
@Component
class CircuitBreakerWrapper(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry,
    private val timeLimiterRegistry: TimeLimiterRegistry,
    private val bulkheadRegistry: BulkheadRegistry,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 리스너 중복 등록 방지 — CB / Bulkhead 이름별로 등록 여부 추적
    private val registeredListeners = ConcurrentHashMap.newKeySet<String>()
    private val registeredBulkheadListeners = ConcurrentHashMap.newKeySet<String>()

    /**
     * 주어진 서비스 이름의 Bulkhead + CB + Retry + TimeLimiter로 suspend 함수를 실행한다.
     * Bulkhead 가득 참 또는 CB OPEN 상태일 때 fallback 값을 반환하고 onFallback 콜백을 호출한다.
     *
     * @param name resilience4j 인스턴스에 등록된 이름
     * @param fallback Bulkhead FULL / CB OPEN 시 반환할 기본값 (주로 emptyList())
     * @param onFallback fallback 사용 시 실행할 콜백 (예: 응답 헤더 설정)
     * @param block 보호할 suspend 함수
     * @return block 의 반환값 또는 fallback
     */
    suspend fun <T> executeWithFallback(
        name: String,
        fallback: T,
        onFallback: suspend () -> Unit = {},
        block: suspend () -> T,
    ): T {
        return try {
            execute(name, block)
        } catch (e: ServiceUnavailableException) {
            log.info("[CircuitBreaker] {} — fallback 반환", name)
            onFallback()
            fallback
        } catch (e: BulkheadFullException) {
            log.warn("[Bulkhead] {} — 동시 호출 한도 초과, fallback 반환", name)
            onFallback()
            fallback
        }
    }

    /**
     * 주어진 서비스 이름에 대해 Bulkhead → TimeLimiter → Retry → CircuitBreaker 순으로 suspend 함수를 실행한다.
     *
     * @param serviceName resilience4j 인스턴스에 등록된 이름 (예: "identity-service")
     * @param block 보호할 suspend 함수
     * @return block 의 반환값
     * @throws BulkheadFullException Bulkhead 동시 호출 한도 초과 시
     * @throws ServiceUnavailableException Circuit Breaker가 OPEN 상태일 때
     * @throws TimeoutException TimeLimiter 타임아웃 초과 시
     */
    suspend fun <T> execute(serviceName: String, block: suspend () -> T): T {
        val bulkhead = bulkheadRegistry.bulkhead(serviceName)
        registerBulkheadIfNeeded(bulkhead)

        // Bulkhead가 가장 바깥 레이어 — 동시 호출 수 초과 시 즉시 BulkheadFullException
        return bulkhead.executeSuspendFunction {
            executeInner(serviceName, block)
        }
    }

    /**
     * Bulkhead 내부에서 TimeLimiter → Retry → CircuitBreaker 순으로 실행한다.
     *
     * withTimeout으로 TimeLimiter를 구현한다 (coroutine-aware — delay/IO 취소 가능).
     * Retry는 suspend 루프로 직접 구현하여 withTimeout 취소 전파를 보장한다.
     * TimeoutCancellationException → java.util.concurrent.TimeoutException 변환.
     */
    private suspend fun <T> executeInner(serviceName: String, block: suspend () -> T): T {
        val cb = circuitBreakerRegistry.circuitBreaker(serviceName)
        val retryConfig = retryRegistry.retry(serviceName).retryConfig
        val timeLimiter = timeLimiterRegistry.timeLimiter(serviceName)
        registerIfNeeded(cb)

        val timeoutMs = timeLimiter.timeLimiterConfig.timeoutDuration.toMillis()
        val maxAttempts = retryConfig.maxAttempts
        val waitDuration = retryConfig.intervalFunction

        return try {
            withTimeout(timeoutMs) {
                // Retry 루프를 suspend로 직접 구현 — withTimeout 취소가 block()까지 전파됨
                var lastException: Exception? = null
                for (attempt in 1..maxAttempts) {
                    try {
                        return@withTimeout executeSuspendWithCircuitBreaker(cb, block)
                    } catch (e: ServiceUnavailableException) {
                        // CB OPEN → 재시도하지 않고 즉시 전파
                        throw e
                    } catch (e: Exception) {
                        lastException = e
                        if (attempt < maxAttempts && isRetryable(e)) {
                            val delayMs = waitDuration?.apply(attempt) ?: 0L
                            if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
                        } else {
                            throw e
                        }
                    }
                }
                throw lastException!!
            }
        } catch (e: TimeoutCancellationException) {
            // coroutines TimeoutCancellationException → java TimeoutException으로 변환
            throw TimeoutException("TimeLimiter '$serviceName' timed out after ${timeoutMs}ms")
        }
    }

    /**
     * 재시도 가능한 예외인지 판별.
     * CB 실패로 기록되는 인프라 장애(shouldRecord)만 재시도한다.
     * ServiceUnavailableException(CB OPEN)과 비즈니스 에러는 재시도하지 않는다.
     */
    private fun isRetryable(e: Exception): Boolean = shouldRecord(e)

    /**
     * CircuitBreaker로 suspend 함수를 실행한다.
     * suspend 컨텍스트에서 직접 호출되므로 withTimeout 취소가 정상 전파된다.
     */
    private suspend fun <T> executeSuspendWithCircuitBreaker(cb: CircuitBreaker, block: suspend () -> T): T {
        if (!cb.tryAcquirePermission()) {
            log.warn("[CircuitBreaker] {} is OPEN — 요청 거부됨", cb.name)
            throw ServiceUnavailableException(
                cb.name,
                io.github.resilience4j.circuitbreaker.CallNotPermittedException.createCallNotPermittedException(cb),
            )
        }

        val start = System.nanoTime()
        return try {
            val result = block()
            cb.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS)
            result
        } catch (e: Exception) {
            val duration = System.nanoTime() - start
            // 인프라 장애만 CB 실패로 기록 — 비즈니스 에러는 기록하지 않음
            if (shouldRecord(e)) {
                cb.onError(duration, TimeUnit.NANOSECONDS, e)
            } else {
                cb.onSuccess(duration, TimeUnit.NANOSECONDS)
            }
            throw e
        }
    }

    /**
     * CB에 기록할 예외인지 판별 — 인프라 장애만 기록.
     * 비즈니스 에러(NOT_FOUND, ALREADY_EXISTS, PERMISSION_DENIED 등)는 제외한다.
     */
    private fun shouldRecord(e: Throwable): Boolean {
        if (e is StatusException || e is StatusRuntimeException) {
            val status = if (e is StatusException) e.status else (e as StatusRuntimeException).status
            return status.code in listOf(
                Status.Code.UNAVAILABLE,
                Status.Code.DEADLINE_EXCEEDED,
                Status.Code.INTERNAL,
                Status.Code.RESOURCE_EXHAUSTED,
            )
        }
        return e is TimeoutException
    }

    /**
     * Circuit Breaker 상태 전이 이벤트를 로깅한다.
     * 동일 CB 이름에 대해 최초 1회만 등록한다.
     */
    private fun registerIfNeeded(cb: CircuitBreaker) {
        if (registeredListeners.add(cb.name)) {
            cb.eventPublisher.onStateTransition { event ->
                log.warn(
                    "[CircuitBreaker] {} 상태 전이: {} → {}",
                    event.circuitBreakerName,
                    event.stateTransition.fromState,
                    event.stateTransition.toState,
                )
            }
        }
    }

    /**
     * Bulkhead 호출 거부 이벤트를 로깅한다.
     * 동일 Bulkhead 이름에 대해 최초 1회만 등록한다.
     */
    private fun registerBulkheadIfNeeded(bulkhead: Bulkhead) {
        if (registeredBulkheadListeners.add(bulkhead.name)) {
            bulkhead.eventPublisher.onCallRejected { event ->
                log.warn("[Bulkhead] {} — 동시 호출 한도 초과로 요청 거부됨", event.bulkheadName)
            }
        }
    }
}
