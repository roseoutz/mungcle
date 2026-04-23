package com.mungcle.gateway.infrastructure.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * gRPC 호출을 Resilience4j Circuit Breaker로 감싸는 유틸리티.
 * Circuit Breaker 상태 전이(CLOSED→OPEN 등)를 로깅하고,
 * 오픈 상태에서 호출 시 ServiceUnavailableException을 발생시킨다.
 */
@Component
class CircuitBreakerWrapper(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 주어진 서비스 이름의 Circuit Breaker로 suspend 함수를 실행한다.
     *
     * @param serviceName resilience4j.circuitbreaker.instances 에 등록된 이름 (예: "identity-service")
     * @param block 보호할 suspend 함수
     * @return block 의 반환값
     * @throws ServiceUnavailableException Circuit Breaker가 OPEN 상태일 때
     */
    suspend fun <T> execute(serviceName: String, block: suspend () -> T): T {
        val cb = circuitBreakerRegistry.circuitBreaker(serviceName)
        registerStateTransitionListener(cb)
        return try {
            cb.executeSuspendFunction { block() }
        } catch (e: io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            log.warn("[CircuitBreaker] {} is OPEN — 요청 거부됨", serviceName)
            throw ServiceUnavailableException(serviceName, e)
        }
    }

    /**
     * Circuit Breaker 상태 전이 이벤트를 로깅한다.
     * 동일 인스턴스에 리스너가 중복 등록되지 않도록 이벤트 발행자 체크.
     */
    private fun registerStateTransitionListener(cb: CircuitBreaker) {
        cb.eventPublisher.onStateTransition { event ->
            log.info(
                "[CircuitBreaker] {} 상태 전이: {} → {}",
                event.circuitBreakerName,
                event.stateTransition.fromState,
                event.stateTransition.toState,
            )
        }
    }
}
