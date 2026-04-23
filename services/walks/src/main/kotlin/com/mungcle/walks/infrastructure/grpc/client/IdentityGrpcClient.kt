package com.mungcle.walks.infrastructure.grpc.client

import com.mungcle.common.grpc.resilience.GrpcCircuitBreakerWrapper
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.getBlockedUserIdsRequest
import com.mungcle.walks.domain.port.out.IdentityPort
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class IdentityGrpcClient(
    @GrpcClient("identity")
    private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
    private val circuitBreaker: GrpcCircuitBreakerWrapper,
) : IdentityPort {

    override suspend fun getBlockedUserIds(userId: Long): List<Long> {
        val request = getBlockedUserIdsRequest { this.userId = userId }
        // identity-service CB — 인프라 장애 시 OPEN 상태로 전환
        return circuitBreaker.execute("identity-service") {
            stub.getBlockedUserIds(request).blockedUserIdsList
        }
    }
}
