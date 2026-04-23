package com.mungcle.social.infrastructure.grpc.client

import com.mungcle.common.grpc.resilience.GrpcCircuitBreakerWrapper
import com.mungcle.proto.walks.v1.WalksServiceGrpcKt
import com.mungcle.proto.walks.v1.getWalkRequest
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.port.out.WalksPort
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class WalksGrpcClient(
    @GrpcClient("walks")
    private val stub: WalksServiceGrpcKt.WalksServiceCoroutineStub,
    private val circuitBreaker: GrpcCircuitBreakerWrapper,
) : WalksPort {

    override suspend fun getWalk(walkId: Long): WalksPort.WalkInfo {
        // walks-service CB — 인프라 장애 시 OPEN 상태로 전환
        // NOT_FOUND는 비즈니스 에러이므로 CB 실패로 기록되지 않음
        return circuitBreaker.execute("walks-service") {
            try {
                val response = stub.getWalk(getWalkRequest { this.walkId = walkId })
                WalksPort.WalkInfo(
                    walkId = response.id,
                    userId = response.userId,
                    dogId = response.dogId,
                )
            } catch (e: StatusException) {
                if (e.status.code == Status.Code.NOT_FOUND) {
                    throw GreetingNotFoundException(walkId)
                }
                throw e
            }
        }
    }
}
