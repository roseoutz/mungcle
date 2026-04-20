package com.mungcle.walks.infrastructure.grpc.client

import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.getBlockedUserIdsRequest
import com.mungcle.walks.domain.port.out.IdentityPort
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class IdentityGrpcClient(
    @GrpcClient("identity")
    private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
) : IdentityPort {

    override suspend fun getBlockedUserIds(userId: Long): List<Long> {
        val request = getBlockedUserIdsRequest { this.userId = userId }
        return stub.getBlockedUserIds(request).blockedUserIdsList
    }
}
