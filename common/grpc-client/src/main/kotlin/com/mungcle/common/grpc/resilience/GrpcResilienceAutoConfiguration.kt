package com.mungcle.common.grpc.resilience

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * GrpcCircuitBreakerWrapper 자동 구성.
 * common:grpc-client 를 의존하는 서비스에서 CircuitBreakerRegistry 빈이 있으면 자동으로 등록된다.
 */
@AutoConfiguration
@ConditionalOnClass(CircuitBreakerRegistry::class)
class GrpcResilienceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun grpcCircuitBreakerWrapper(registry: CircuitBreakerRegistry): GrpcCircuitBreakerWrapper =
        GrpcCircuitBreakerWrapper(registry)
}
