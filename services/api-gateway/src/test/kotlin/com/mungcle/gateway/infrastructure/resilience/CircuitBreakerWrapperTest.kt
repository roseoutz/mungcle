package com.mungcle.gateway.infrastructure.resilience

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.test.assertEquals

class CircuitBreakerWrapperTest {

    private lateinit var registry: CircuitBreakerRegistry
    private lateinit var wrapper: CircuitBreakerWrapper

    @BeforeEach
    fun setUp() {
        // 테스트용 Circuit Breaker 설정 — 빠른 오픈을 위해 minimumNumberOfCalls=3, failureRateThreshold=50
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(4)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofMillis(100))
            .permittedNumberOfCallsInHalfOpenState(2)
            .recordExceptions(StatusException::class.java, RuntimeException::class.java)
            .build()

        registry = CircuitBreakerRegistry.of(config)
        wrapper = CircuitBreakerWrapper(registry)
    }

    @Test
    fun `성공 호출 — Circuit Breaker CLOSED 유지`() = runTest {
        // given
        var callCount = 0

        // when: 5번 성공 호출
        repeat(5) {
            wrapper.execute("test-service") { callCount++ }
        }

        // then
        assertEquals(5, callCount)
        assertEquals(
            CircuitBreaker.State.CLOSED,
            registry.circuitBreaker("test-service").state,
        )
    }

    @Test
    fun `실패율 초과 시 Circuit Breaker OPEN`() = runTest {
        // given: minimumNumberOfCalls=3, failureRateThreshold=50% → 3번 중 2번 실패하면 OPEN

        // when: 1번 성공 후 2번 실패
        runCatching { wrapper.execute("test-service") { /* 성공 */ } }
        runCatching {
            wrapper.execute("test-service") {
                throw StatusException(Status.UNAVAILABLE)
            }
        }
        runCatching {
            wrapper.execute("test-service") {
                throw StatusException(Status.INTERNAL)
            }
        }

        // then: Circuit Breaker가 OPEN 상태여야 한다
        assertEquals(
            CircuitBreaker.State.OPEN,
            registry.circuitBreaker("test-service").state,
        )
    }

    @Test
    fun `Circuit Breaker OPEN 상태에서 요청 거부 — ServiceUnavailableException 발생`() = runTest {
        // given: Circuit Breaker를 강제로 OPEN
        registry.circuitBreaker("test-service").transitionToOpenState()

        // when & then
        assertThrows<ServiceUnavailableException> {
            wrapper.execute("test-service") { "should not reach here" }
        }
    }

    @Test
    fun `Circuit Breaker OPEN 상태에서 serviceName이 예외에 포함됨`() = runTest {
        // given
        registry.circuitBreaker("identity-service").transitionToOpenState()

        // when
        val ex = assertThrows<ServiceUnavailableException> {
            wrapper.execute("identity-service") { "irrelevant" }
        }

        // then
        assertEquals("identity-service", ex.serviceName)
    }

    @Test
    fun `waitDuration 이후 HALF_OPEN 전환 — 성공 호출로 CLOSED 복귀`() = runTest {
        // given: waitDurationInOpenState=100ms 설정
        registry.circuitBreaker("test-service").transitionToOpenState()

        // when: 100ms 대기 후 HALF_OPEN
        Thread.sleep(150)
        registry.circuitBreaker("test-service").transitionToHalfOpenState()

        assertEquals(CircuitBreaker.State.HALF_OPEN, registry.circuitBreaker("test-service").state)

        // permittedNumberOfCallsInHalfOpenState=2 — 2번 성공하면 CLOSED
        wrapper.execute("test-service") { /* 성공 1 */ }
        wrapper.execute("test-service") { /* 성공 2 */ }

        // then
        assertEquals(
            CircuitBreaker.State.CLOSED,
            registry.circuitBreaker("test-service").state,
        )
    }

    @Test
    fun `HALF_OPEN 상태에서 실패 시 다시 OPEN`() = runTest {
        // given
        registry.circuitBreaker("test-service").transitionToOpenState()
        Thread.sleep(150)
        registry.circuitBreaker("test-service").transitionToHalfOpenState()

        // when: HALF_OPEN 중 permittedNumberOfCallsInHalfOpenState(2)번 모두 실패
        // — CB는 모든 허용 호출이 완료된 후 실패율을 평가해 OPEN으로 전환한다
        repeat(2) {
            runCatching {
                wrapper.execute("test-service") {
                    throw StatusException(Status.UNAVAILABLE)
                }
            }
        }

        // then: 다시 OPEN
        assertEquals(
            CircuitBreaker.State.OPEN,
            registry.circuitBreaker("test-service").state,
        )
    }

    @Test
    fun `CallNotPermittedException 원인 예외가 ServiceUnavailableException에 연결됨`() = runTest {
        // given
        registry.circuitBreaker("test-service").transitionToOpenState()

        // when
        val ex = assertThrows<ServiceUnavailableException> {
            wrapper.execute("test-service") { "irrelevant" }
        }

        // then: cause가 CallNotPermittedException이어야 한다
        assert(ex.cause is CallNotPermittedException)
    }
}
