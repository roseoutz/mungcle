package com.mungcle.gateway.infrastructure.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class CircuitBreakerWrapperTest {

    private lateinit var registry: CircuitBreakerRegistry
    private lateinit var wrapper: CircuitBreakerWrapper

    @BeforeEach
    fun setUp() {
        // recordExceptions는 설정하지 않음 — CircuitBreakerWrapper가 shouldRecord()로 직접 제어
        val config = CircuitBreakerConfig.custom()
            .slidingWindowSize(5)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(100f) // 100%로 설정하여 5번 실패 후 즉시 오픈
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build()
        registry = CircuitBreakerRegistry.of(config)
        wrapper = CircuitBreakerWrapper(registry)
    }

    @Test
    fun `성공 호출은 결과를 반환한다`() = runTest {
        val result = wrapper.execute("test-service") { "ok" }
        assertEquals("ok", result)
    }

    @Test
    fun `임계값 이상 실패 시 Circuit Breaker가 OPEN 상태가 된다`() = runTest {
        val cb = registry.circuitBreaker("threshold-service")

        // 5번 실패 → OPEN 전환
        repeat(5) {
            try {
                wrapper.execute("threshold-service") { throw StatusException(Status.UNAVAILABLE) }
            } catch (_: StatusException) {
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.state)
    }

    @Test
    fun `Circuit Breaker가 OPEN 상태일 때 요청은 ServiceUnavailableException으로 거부된다`() = runTest {
        val cb = registry.circuitBreaker("open-service")
        cb.transitionToOpenState()

        assertThrows(ServiceUnavailableException::class.java) {
            kotlinx.coroutines.runBlocking {
                wrapper.execute("open-service") { "should not reach" }
            }
        }
    }

    @Test
    fun `Circuit Breaker가 CLOSED 상태일 때 성공 호출은 정상 동작한다`() = runTest {
        val cb = registry.circuitBreaker("closed-service")
        assertEquals(CircuitBreaker.State.CLOSED, cb.state)

        val result = wrapper.execute("closed-service") { 42 }
        assertEquals(42, result)
    }

    @Test
    fun `비즈니스 에러(NOT_FOUND)는 CB 실패로 기록되지 않아 OPEN 상태가 되지 않는다`() = runTest {
        val cb = registry.circuitBreaker("business-error-service")

        // NOT_FOUND 5번 던져도 CB는 CLOSED 유지
        repeat(5) {
            try {
                wrapper.execute("business-error-service") { throw StatusException(Status.NOT_FOUND) }
            } catch (_: StatusException) {
            }
        }

        assertEquals(CircuitBreaker.State.CLOSED, cb.state)
    }

    @Test
    fun `executeWithFallback — CB CLOSED 시 실제 block 결과와 isFallback=false 반환`() = runTest {
        val result = wrapper.executeWithFallback("fallback-closed-service", emptyList<String>()) {
            listOf("a", "b")
        }

        assertEquals(listOf("a", "b"), result.value)
        assertFalse(result.isFallback)
    }

    @Test
    fun `executeWithFallback — CB OPEN 시 fallback 값과 isFallback=true 반환`() = runTest {
        val cb = registry.circuitBreaker("fallback-open-service")
        cb.transitionToOpenState()

        val result = wrapper.executeWithFallback("fallback-open-service", emptyList<String>()) {
            listOf("should not reach")
        }

        assertEquals(emptyList<String>(), result.value)
        assertTrue(result.isFallback)
    }
}
