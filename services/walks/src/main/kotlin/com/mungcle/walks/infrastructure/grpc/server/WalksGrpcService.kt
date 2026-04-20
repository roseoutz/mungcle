package com.mungcle.walks.infrastructure.grpc.server

import com.mungcle.proto.walks.v1.GetMyActiveWalksRequest
import com.mungcle.proto.walks.v1.GetMyActiveWalksResponse
import com.mungcle.proto.walks.v1.GetNearbyPatternsRequest
import com.mungcle.proto.walks.v1.GetNearbyPatternsResponse
import com.mungcle.proto.walks.v1.GetNearbyWalksRequest
import com.mungcle.proto.walks.v1.GetNearbyWalksResponse
import com.mungcle.proto.walks.v1.GetWalkGridCellRequest
import com.mungcle.proto.walks.v1.GetWalkGridCellResponse
import com.mungcle.proto.walks.v1.StartWalkRequest
import com.mungcle.proto.walks.v1.StopWalkRequest
import com.mungcle.proto.walks.v1.WalkInfo
import com.mungcle.proto.walks.v1.WalkStatus as ProtoWalkStatus
import com.mungcle.proto.walks.v1.WalkType as ProtoWalkType
import com.mungcle.proto.walks.v1.WalksServiceGrpcKt
import com.mungcle.proto.walks.v1.getMyActiveWalksResponse
import com.mungcle.proto.walks.v1.getNearbyWalksResponse
import com.mungcle.proto.walks.v1.getWalkGridCellResponse
import com.mungcle.proto.walks.v1.nearbyWalkInfo
import com.mungcle.proto.walks.v1.walkInfo
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.GetMyActiveWalksUseCase
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.`in`.GetWalkGridCellUseCase
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.`in`.StopWalkUseCase
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class WalksGrpcService(
    private val startWalkUseCase: StartWalkUseCase,
    private val stopWalkUseCase: StopWalkUseCase,
    private val getNearbyWalksUseCase: GetNearbyWalksUseCase,
    private val getMyActiveWalksUseCase: GetMyActiveWalksUseCase,
    private val getWalkGridCellUseCase: GetWalkGridCellUseCase,
) : WalksServiceGrpcKt.WalksServiceCoroutineImplBase() {

    override suspend fun startWalk(request: StartWalkRequest): WalkInfo {
        val walkType = when (request.type) {
            ProtoWalkType.WALK_TYPE_OPEN -> WalkType.OPEN
            ProtoWalkType.WALK_TYPE_SOLO -> WalkType.SOLO
            else -> throw StatusException(
                Status.INVALID_ARGUMENT.withDescription("유효하지 않은 산책 타입입니다")
            )
        }
        val walk = startWalkUseCase.execute(
            StartWalkUseCase.Command(
                userId = request.userId,
                dogId = request.dogId,
                type = walkType,
                lat = request.lat,
                lng = request.lng,
            )
        )
        return walk.toWalkInfo()
    }

    override suspend fun stopWalk(request: StopWalkRequest): WalkInfo {
        val walk = stopWalkUseCase.execute(
            walkId = request.walkId,
            userId = request.userId,
        )
        return walk.toWalkInfo()
    }

    override suspend fun getNearbyWalks(request: GetNearbyWalksRequest): GetNearbyWalksResponse {
        val results = getNearbyWalksUseCase.execute(
            gridCell = request.gridCell,
            userId = request.userId,
            blockedUserIds = request.blockedUserIdsList,
        )
        return getNearbyWalksResponse {
            this.walks += results.map { result ->
                nearbyWalkInfo {
                    walkId = result.walk.id
                    dogId = result.walk.dogId
                    userId = result.walk.userId
                    gridDistance = result.gridDistance
                    startedAt = result.walk.startedAt.epochSecond
                }
            }
        }
    }

    override suspend fun getMyActiveWalks(request: GetMyActiveWalksRequest): GetMyActiveWalksResponse {
        val walks = getMyActiveWalksUseCase.execute(request.userId)
        return getMyActiveWalksResponse {
            this.walks += walks.map { it.toWalkInfo() }
        }
    }

    override suspend fun getWalkGridCell(request: GetWalkGridCellRequest): GetWalkGridCellResponse {
        val gridCell = getWalkGridCellUseCase.execute(request.walkId)
        return getWalkGridCellResponse {
            this.gridCell = gridCell
        }
    }

    override suspend fun getNearbyPatterns(request: GetNearbyPatternsRequest): GetNearbyPatternsResponse {
        throw StatusException(
            Status.UNIMPLEMENTED.withDescription("GetNearbyPatterns는 Task 05에서 구현 예정")
        )
    }

    private fun Walk.toWalkInfo(): WalkInfo = walkInfo {
        id = this@toWalkInfo.id
        dogId = this@toWalkInfo.dogId
        userId = this@toWalkInfo.userId
        type = when (this@toWalkInfo.type) {
            WalkType.OPEN -> ProtoWalkType.WALK_TYPE_OPEN
            WalkType.SOLO -> ProtoWalkType.WALK_TYPE_SOLO
        }
        gridCell = this@toWalkInfo.gridCell
        status = when (this@toWalkInfo.status) {
            WalkStatus.ACTIVE -> ProtoWalkStatus.WALK_STATUS_ACTIVE
            WalkStatus.ENDED -> ProtoWalkStatus.WALK_STATUS_ENDED
        }
        startedAt = this@toWalkInfo.startedAt.epochSecond
        endsAt = this@toWalkInfo.endsAt.epochSecond
    }
}
