package com.mungcle.social.infrastructure.grpc.client

import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.isBlockedRequest
import com.mungcle.social.domain.port.out.IdentityPort
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class IdentityGrpcClient(
    @GrpcClient("identity")
    private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
) : IdentityPort {

    override suspend fun isBlocked(userId1: Long, userId2: Long): Boolean {
        val request = isBlockedRequest {
            userIdA = userId1
            userIdB = userId2
        }
        return stub.isBlocked(request).blocked
    }
}
