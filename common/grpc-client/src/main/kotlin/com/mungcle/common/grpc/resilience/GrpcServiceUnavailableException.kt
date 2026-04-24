package com.mungcle.common.grpc.resilience

import io.github.resilience4j.circuitbreaker.CallNotPermittedException

/**
 * Circuit Breaker가 OPEN 상태일 때 발생하는 예외.
 * 각 서비스의 gRPC 서버 구현에서 gRPC UNAVAILABLE 상태로 변환된다.
 *
 * @param serviceName 사용 불가 상태인 백엔드 서비스 이름
 * @param cause 원인 예외 (CallNotPermittedException)
 */
class GrpcServiceUnavailableException(
    val serviceName: String,
    cause: CallNotPermittedException,
) : RuntimeException("Service unavailable: $serviceName — circuit breaker is OPEN", cause)
