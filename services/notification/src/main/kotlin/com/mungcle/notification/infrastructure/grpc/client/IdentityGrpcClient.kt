package com.mungcle.notification.infrastructure.grpc.client

import com.mungcle.common.grpc.resilience.GrpcCircuitBreakerWrapper
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.getPushTokenRequest
import com.mungcle.notification.domain.port.out.UserPushTokenPort
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * identity 서비스에서 사용자 FCM 푸시 토큰을 조회하는 gRPC 클라이언트.
 */
@Component
class IdentityGrpcClient(
    @GrpcClient("identity")
    private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
    private val circuitBreaker: GrpcCircuitBreakerWrapper,
) : UserPushTokenPort {

    private val log = LoggerFactory.getLogger(IdentityGrpcClient::class.java)

    override suspend fun getPushToken(userId: Long): String? {
        val request = getPushTokenRequest { this.userId = userId }
        return try {
            circuitBreaker.execute("identity-service") {
                val response = stub.getPushToken(request)
                response.pushToken.ifBlank { null }
            }
        } catch (e: Exception) {
            log.warn("identity 서비스에서 pushToken 조회 실패: userId={}, error={}", userId, e.message)
            null
        }
    }
}
