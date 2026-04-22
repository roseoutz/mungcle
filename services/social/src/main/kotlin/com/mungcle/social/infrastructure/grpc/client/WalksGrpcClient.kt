package com.mungcle.social.infrastructure.grpc.client

import com.mungcle.social.domain.port.out.WalksPort
import org.springframework.stereotype.Component

/**
 * WalksPort stub 구현체.
 * walks proto에 단건 Walk 조회 RPC가 없으므로 태스크 08에서 완성 예정.
 * 현재는 GreetingNotFoundException을 던지는 stub으로 대체한다.
 */
@Component
class WalksGrpcClient : WalksPort {

    override suspend fun getWalk(walkId: Long): WalksPort.WalkInfo {
        throw UnsupportedOperationException(
            "WalksGrpcClient.getWalk is not yet implemented — walks proto lacks a GetWalk RPC. " +
                "Implement in task-08 when GetWalk is added to walks.proto."
        )
    }
}
