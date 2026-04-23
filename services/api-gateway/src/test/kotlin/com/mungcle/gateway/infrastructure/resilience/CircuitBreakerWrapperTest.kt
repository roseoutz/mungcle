package com.mungcle.gateway.infrastructure.resilience

import io.github.resilience4j.bulkhead.BulkheadConfig
import io.github.resilience4j.bulkhead.BulkheadFullException
import io.github.resilience4j.bulkhead.BulkheadRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class CircuitBreakerWrapperTest {

    private lateinit var cbRegistry: CircuitBreakerRegistry
    private lateinit var retryRegistry: RetryRegistry
    private lateinit var timeLimiterRegistry: TimeLimiterRegistry
    private lateinit var bulkheadRegistry: BulkheadRegistry
    private lateinit var wrapper: CircuitBreakerWrapper

    @BeforeEach
    fun setUp() {
        // recordExceptions는 설정하지 않음 — CircuitBreakerWrapper가 shouldRecord()로 직접 제어
        val cbConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(5)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(100f) // 100%로 설정하여 5번 실패 후 즉시 오픈
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build()
        cbRegistry = CircuitBreakerRegistry.of(cbConfig)

        // 재시도 대기 없이 즉시 재시도 — 테스트 속도를 위해 waitDuration=0
        val retryConfig = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(0))
            .retryExceptions(StatusException::class.java, StatusRuntimeException::class.java)
            .ignoreExceptions(ServiceUnavailableException::class.java)
            .build()
        retryRegistry = RetryRegistry.of(retryConfig)

        // 타임아웃을 충분히 길게 설정 — 기본 동작 테스트에서는 타임아웃이 발생하지 않도록
        val timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .cancelRunningFuture(true)
            .build()
        timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig)

        // 충분히 높은 동시 호출 수 — 테스트에서 Bulkhead로 인한 거부가 발생하지 않도록
        val bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(100)
            .maxWaitDuration(Duration.ofMillis(0))
            .build()
        bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig)

        wrapper = CircuitBreakerWrapper(cbRegistry, retryRegistry, timeLimiterRegistry, bulkheadRegistry)
    }

    @Test
    fun `성공 호출은 결과를 반환한다`() = runTest {
        val result = wrapper.execute("test-service") { "ok" }
        assertEquals("ok", result)
    }

    @Test
    fun `임계값 이상 실패 시 Circuit Breaker가 OPEN 상태가 된다`() = runTest {
        val cb = cbRegistry.circuitBreaker("threshold-service")

        // Retry가 각 호출을 최대 3회 재시도하지만, UNAVAILABLE은 CB 실패로 기록됨
        // 5번 실패(CB 기록 기준) → OPEN 전환
        repeat(5) {
            try {
                wrapper.execute("threshold-service") { throw StatusException(Status.UNAVAILABLE) }
            } catch (_: Exception) {
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.state)
    }

    @Test
    fun `Circuit Breaker가 OPEN 상태일 때 요청은 ServiceUnavailableException으로 거부된다`() = runTest {
        val cb = cbRegistry.circuitBreaker("open-service")
        cb.transitionToOpenState()

        assertThrows(ServiceUnavailableException::class.java) {
            kotlinx.coroutines.runBlocking {
                wrapper.execute("open-service") { "should not reach" }
            }
        }
    }

    @Test
    fun `Circuit Breaker가 CLOSED 상태일 때 성공 호출은 정상 동작한다`() = runTest {
        val cb = cbRegistry.circuitBreaker("closed-service")
        assertEquals(CircuitBreaker.State.CLOSED, cb.state)

        val result = wrapper.execute("closed-service") { 42 }
        assertEquals(42, result)
    }

    @Test
    fun `비즈니스 에러(NOT_FOUND)는 CB 실패로 기록되지 않아 OPEN 상태가 되지 않는다`() = runTest {
        val cb = cbRegistry.circuitBreaker("business-error-service")

        // NOT_FOUND 5번 던져도 CB는 CLOSED 유지 (Retry도 비즈니스 에러는 재시도하지 않음)
        repeat(5) {
            try {
                wrapper.execute("business-error-service") { throw StatusException(Status.NOT_FOUND) }
            } catch (_: StatusException) {
            }
        }

        assertEquals(CircuitBreaker.State.CLOSED, cb.state)
    }

    @Test
    fun `일시적 장애 후 성공 — Retry가 재시도하여 최종 성공을 반환한다`() = runTest {
        val callCount = AtomicInteger(0)

        // 처음 2회는 UNAVAILABLE 실패, 3번째 호출은 성공
        val result = wrapper.execute("retry-success-service") {
            val attempt = callCount.incrementAndGet()
            if (attempt < 3) throw StatusException(Status.UNAVAILABLE)
            "success-on-attempt-$attempt"
        }

        assertEquals("success-on-attempt-3", result)
        assertEquals(3, callCount.get())
    }

    @Test
    fun `타임아웃 — 설정 시간 초과 시 TimeoutException이 발생한다`() {
        // 타임아웃을 매우 짧게 설정 (10ms)
        val shortTimeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(10))
            .cancelRunningFuture(true)
            .build()
        val shortTimeLimiterRegistry = TimeLimiterRegistry.of(shortTimeLimiterConfig)
        val shortWrapper = CircuitBreakerWrapper(cbRegistry, retryRegistry, shortTimeLimiterRegistry, bulkheadRegistry)

        assertThrows(TimeoutException::class.java) {
            kotlinx.coroutines.runBlocking {
                shortWrapper.execute("timeout-service") {
                    // 100ms 대기 — 10ms 타임아웃보다 길어서 TimeoutException 발생
                    kotlinx.coroutines.delay(100)
                    "should not return"
                }
            }
        }
    }

    @Test
    fun `CB OPEN 상태에서 Retry를 시도하지 않는다 — ServiceUnavailableException은 재시도 제외 대상`() = runTest {
        val cb = cbRegistry.circuitBreaker("cb-open-no-retry-service")
        cb.transitionToOpenState()

        val callCount = AtomicInteger(0)

        assertThrows(ServiceUnavailableException::class.java) {
            kotlinx.coroutines.runBlocking {
                wrapper.execute("cb-open-no-retry-service") {
                    callCount.incrementAndGet()
                    "should not reach"
                }
            }
        }

        // block 자체는 한 번도 실행되지 않아야 함 (CB OPEN → 즉시 거부, Retry 없음)
        assertEquals(0, callCount.get())
    }

    // ── Bulkhead 테스트 ──────────────────────────────────────────────────────

    @Test
    fun `Bulkhead 한도 내 동시 호출은 성공한다`() = runTest {
        // maxConcurrentCalls=100인 기본 bulkheadRegistry 사용 — 3개 호출은 모두 통과
        val results = (1..3).map { i ->
            wrapper.execute("bulkhead-ok-service") { "result-$i" }
        }
        assertEquals(3, results.size)
        assertTrue(results.all { it.startsWith("result-") })
    }

    @Test
    fun `Bulkhead 한도 초과 호출은 BulkheadFullException을 던진다`() {
        // maxConcurrentCalls=1, maxWaitDuration=0 — 동시에 2번째 호출 시 즉시 거부
        val tightBulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(1)
            .maxWaitDuration(Duration.ofMillis(0))
            .build()
        val tightBulkheadRegistry = BulkheadRegistry.of(tightBulkheadConfig)
        val bulkhead = tightBulkheadRegistry.bulkhead("tight-service")

        // 슬롯 1개를 직접 점유하여 포화 상태로 만든 뒤 execute 호출
        bulkhead.acquirePermission()

        val tightWrapper = CircuitBreakerWrapper(cbRegistry, retryRegistry, timeLimiterRegistry, tightBulkheadRegistry)

        assertThrows(BulkheadFullException::class.java) {
            kotlinx.coroutines.runBlocking {
                tightWrapper.execute("tight-service") { "should not reach" }
            }
        }
    }

    // ── executeWithFallback 테스트 ───────────────────────────────────────────

    @Test
    fun `executeWithFallback — CB CLOSED 시 block 결과를 반환한다`() = runTest {
        val result = wrapper.executeWithFallback("fallback-closed-service", emptyList<String>()) {
            listOf("real-data")
        }
        assertEquals(listOf("real-data"), result)
    }

    @Test
    fun `executeWithFallback — CB OPEN 시 fallback 값을 반환한다`() = runTest {
        val cb = cbRegistry.circuitBreaker("fallback-open-service")
        cb.transitionToOpenState()

        val result = wrapper.executeWithFallback("fallback-open-service", emptyList<String>()) {
            listOf("should-not-reach")
        }
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `executeWithFallback — CB OPEN 시 onFallback 콜백이 호출된다`() = runTest {
        val cb = cbRegistry.circuitBreaker("fallback-callback-service")
        cb.transitionToOpenState()

        var callbackInvoked = false
        wrapper.executeWithFallback(
            name = "fallback-callback-service",
            fallback = emptyList<String>(),
            onFallback = { callbackInvoked = true },
        ) { listOf("should-not-reach") }

        assertTrue(callbackInvoked)
    }

    @Test
    fun `executeWithFallback — CB CLOSED 시 onFallback 콜백은 호출되지 않는다`() = runTest {
        var callbackInvoked = false
        val result = wrapper.executeWithFallback(
            name = "fallback-not-triggered-service",
            fallback = emptyList<String>(),
            onFallback = { callbackInvoked = true },
        ) { listOf("real-data") }

        assertEquals(listOf("real-data"), result)
        assertEquals(false, callbackInvoked)
    }

    @Test
    fun `서비스별 Bulkhead는 독립적으로 동작한다 — 한 서비스 포화가 다른 서비스에 영향 없음`() {
        val tightBulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(1)
            .maxWaitDuration(Duration.ofMillis(0))
            .build()
        val tightBulkheadRegistry = BulkheadRegistry.of(tightBulkheadConfig)
        val tightWrapper = CircuitBreakerWrapper(cbRegistry, retryRegistry, timeLimiterRegistry, tightBulkheadRegistry)

        // service-a의 슬롯을 모두 점유
        val bulkheadA = tightBulkheadRegistry.bulkhead("service-a")
        bulkheadA.acquirePermission()

        // service-a는 거부
        assertThrows(BulkheadFullException::class.java) {
            kotlinx.coroutines.runBlocking {
                tightWrapper.execute("service-a") { "should not reach" }
            }
        }

        // service-b는 독립적이므로 정상 통과
        val result = kotlinx.coroutines.runBlocking {
            tightWrapper.execute("service-b") { "service-b-ok" }
        }
        assertEquals("service-b-ok", result)
    }
}
