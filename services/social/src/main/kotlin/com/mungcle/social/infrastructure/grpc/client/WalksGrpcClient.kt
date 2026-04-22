package com.mungcle.social.infrastructure.grpc.client

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
) : WalksPort {

    override suspend fun getWalk(walkId: Long): WalksPort.WalkInfo {
        try {
            val response = stub.getWalk(getWalkRequest { this.walkId = walkId })
            return WalksPort.WalkInfo(
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
