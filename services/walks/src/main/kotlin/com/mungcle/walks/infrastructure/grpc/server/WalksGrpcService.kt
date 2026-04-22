package com.mungcle.walks.infrastructure.grpc.server

import com.mungcle.walks.domain.exception.WalkException
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.GetMyActiveWalksUseCase
import com.mungcle.walks.domain.port.`in`.GetNearbyPatternsUseCase
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.`in`.GetWalkGridCellUseCase
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.`in`.StopWalkUseCase
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
import com.mungcle.proto.walks.v1.WalksServiceGrpcKt
import com.mungcle.proto.walks.v1.getMyActiveWalksResponse
import com.mungcle.proto.walks.v1.getNearbyPatternsResponse
import com.mungcle.proto.walks.v1.getNearbyWalksResponse
import com.mungcle.proto.walks.v1.getWalkGridCellResponse
import com.mungcle.proto.walks.v1.nearbyWalkInfo
import com.mungcle.proto.walks.v1.walkInfo
import com.mungcle.proto.walks.v1.walkPatternInfo
import com.mungcle.walks.domain.model.Walk
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
    private val getNearbyPatternsUseCase: GetNearbyPatternsUseCase,
) : WalksServiceGrpcKt.WalksServiceCoroutineImplBase() {

    override suspend fun startWalk(request: StartWalkRequest): WalkInfo {
        val walkType = when (request.type) {
            com.mungcle.proto.walks.v1.WalkType.WALK_TYPE_OPEN -> WalkType.OPEN
            com.mungcle.proto.walks.v1.WalkType.WALK_TYPE_SOLO -> WalkType.SOLO
            else -> throw StatusException(
                Status.INVALID_ARGUMENT.withDescription("유효하지 않은 산책 타입입니다")
            )
        }
        try {
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
        } catch (e: WalkException) {
            throw e.toStatusException()
        }
    }

    override suspend fun stopWalk(request: StopWalkRequest): WalkInfo {
        try {
            val walk = stopWalkUseCase.execute(
                StopWalkUseCase.Command(
                    walkId = request.walkId,
                    userId = request.userId,
                )
            )
            return walk.toWalkInfo()
        } catch (e: WalkException) {
            throw e.toStatusException()
        }
    }

    override suspend fun getNearbyWalks(request: GetNearbyWalksRequest): GetNearbyWalksResponse {
        try {
            val results = getNearbyWalksUseCase.execute(
                GetNearbyWalksUseCase.Query(
                    gridCell = request.gridCell,
                    userId = request.userId,
                    blockedUserIds = request.blockedUserIdsList,
                )
            )
            return getNearbyWalksResponse {
                walks += results.map { info ->
                    nearbyWalkInfo {
                        walkId = info.walkId
                        dogId = info.dogId
                        userId = info.userId
                        gridDistance = info.gridDistance
                        startedAt = info.startedAt.epochSecond
                    }
                }
            }
        } catch (e: WalkException) {
            throw e.toStatusException()
        }
    }

    override suspend fun getMyActiveWalks(request: GetMyActiveWalksRequest): GetMyActiveWalksResponse {
        val walks = getMyActiveWalksUseCase.execute(request.userId)
        return getMyActiveWalksResponse {
            this.walks += walks.map { it.toWalkInfo() }
        }
    }

    override suspend fun getWalkGridCell(request: GetWalkGridCellRequest): GetWalkGridCellResponse {
        try {
            val gridCell = getWalkGridCellUseCase.execute(request.walkId)
            return getWalkGridCellResponse {
                this.gridCell = gridCell.value
            }
        } catch (e: WalkException) {
            throw e.toStatusException()
        }
    }

    override suspend fun getNearbyPatterns(request: GetNearbyPatternsRequest): GetNearbyPatternsResponse {
        try {
            val results = getNearbyPatternsUseCase.execute(
                GetNearbyPatternsUseCase.Query(
                    gridCell = request.gridCell,
                    userId = request.userId,
                    blockedUserIds = request.blockedUserIdsList,
                )
            )
            return getNearbyPatternsResponse {
                patterns += results.map { pattern ->
                    walkPatternInfo {
                        dogId = pattern.dogId
                        typicalHour = pattern.hourOfDay
                        countLast14Days = pattern.walkCount
                    }
                }
            }
        } catch (e: WalkException) {
            throw e.toStatusException()
        }
    }

    private fun Walk.toWalkInfo(): WalkInfo = walkInfo {
        id = this@toWalkInfo.id
        dogId = this@toWalkInfo.dogId
        userId = this@toWalkInfo.userId
        type = when (this@toWalkInfo.type) {
            WalkType.OPEN -> com.mungcle.proto.walks.v1.WalkType.WALK_TYPE_OPEN
            WalkType.SOLO -> com.mungcle.proto.walks.v1.WalkType.WALK_TYPE_SOLO
        }
        gridCell = this@toWalkInfo.gridCell.value
        status = when (this@toWalkInfo.status) {
            com.mungcle.walks.domain.model.WalkStatus.ACTIVE -> com.mungcle.proto.walks.v1.WalkStatus.WALK_STATUS_ACTIVE
            com.mungcle.walks.domain.model.WalkStatus.ENDED -> com.mungcle.proto.walks.v1.WalkStatus.WALK_STATUS_ENDED
        }
        startedAt = this@toWalkInfo.startedAt.epochSecond
        endsAt = this@toWalkInfo.endsAt.epochSecond
    }
}
