package com.mungcle.notification.infrastructure.grpc.client

import com.mungcle.notification.domain.port.out.IdentityPort
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.getUserRequest
import io.grpc.StatusException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class IdentityGrpcClient(
    private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
) : IdentityPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun getPushToken(userId: Long): String? {
        return try {
            val request = getUserRequest { this.userId = userId }
            val response = stub.getUser(request)
            response.pushToken.ifBlank { null }
        } catch (e: StatusException) {
            log.warn("Identity gRPC 호출 실패 (userId={}): {}", userId, e.status)
            null // 조회 실패 시 push 포기 (인앱 알림은 유지)
        }
    }
}
