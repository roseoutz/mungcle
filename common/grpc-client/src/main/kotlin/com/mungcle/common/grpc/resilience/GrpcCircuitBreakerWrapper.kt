package com.mungcle.common.grpc.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * 서비스 간 gRPC 호출을 Resilience4j Circuit Breaker로 감싸는 공통 유틸리티.
 * walks, social, notification 서비스의 gRPC 클라이언트에서 사용한다.
 *
 * 비즈니스 에러(NOT_FOUND, ALREADY_EXISTS 등)는 CB 실패로 기록하지 않는다.
 * 인프라 장애(UNAVAILABLE, DEADLINE_EXCEEDED, INTERNAL, RESOURCE_EXHAUSTED)만 기록한다.
 *
 * GrpcResilienceAutoConfiguration 을 통해 Spring Bean으로 등록된다.
 */
class GrpcCircuitBreakerWrapper(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 리스너 중복 등록 방지 — CB 이름별로 등록 여부 추적
    private val registeredListeners = ConcurrentHashMap.newKeySet<String>()

    /**
     * 주어진 서비스 이름의 Circuit Breaker로 suspend 함수를 실행한다.
     *
     * @param serviceName resilience4j.circuitbreaker.instances 에 등록된 이름 (예: "identity-service")
     * @param block 보호할 suspend 함수
     * @return block 의 반환값
     * @throws GrpcServiceUnavailableException Circuit Breaker가 OPEN 상태일 때
     */
    suspend fun <T> execute(serviceName: String, block: suspend () -> T): T {
        val cb = circuitBreakerRegistry.circuitBreaker(serviceName)
        registerIfNeeded(cb)

        if (!cb.tryAcquirePermission()) {
            log.warn("[CircuitBreaker] {} is OPEN — 요청 거부됨", serviceName)
            throw GrpcServiceUnavailableException(
                serviceName,
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
            // 인프라 장애만 CB 실패로 기록 — 비즈니스 에러는 성공으로 기록
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
}
