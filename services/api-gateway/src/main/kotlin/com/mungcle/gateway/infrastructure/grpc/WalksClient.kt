package com.mungcle.gateway.infrastructure.grpc

import com.mungcle.proto.walks.v1.NearbyWalkInfo
import com.mungcle.proto.walks.v1.WalkInfo
import com.mungcle.proto.walks.v1.WalkPatternInfo
import com.mungcle.proto.walks.v1.WalkType
import com.mungcle.proto.walks.v1.WalksServiceGrpcKt
import com.mungcle.proto.walks.v1.getMyActiveWalksRequest
import com.mungcle.proto.walks.v1.getNearbyPatternsRequest
import com.mungcle.proto.walks.v1.getNearbyWalksRequest
import com.mungcle.proto.walks.v1.getWalkGridCellRequest
import com.mungcle.proto.walks.v1.startWalkRequest
import com.mungcle.proto.walks.v1.stopWalkRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class WalksClient(
    @GrpcClient("walks") private val stub: WalksServiceGrpcKt.WalksServiceCoroutineStub,
) {

    suspend fun startWalk(userId: Long, dogId: Long, type: WalkType, lat: Double, lng: Double): WalkInfo =
        stub.startWalk(startWalkRequest {
            this.userId = userId
            this.dogId = dogId
            this.type = type
            this.lat = lat
            this.lng = lng
        })

    suspend fun stopWalk(walkId: Long, userId: Long): WalkInfo =
        stub.stopWalk(stopWalkRequest {
            this.walkId = walkId
            this.userId = userId
        })

    suspend fun getNearbyWalks(gridCell: String, userId: Long, blockedUserIds: List<Long>): List<NearbyWalkInfo> =
        stub.getNearbyWalks(getNearbyWalksRequest {
            this.gridCell = gridCell
            this.userId = userId
            this.blockedUserIds += blockedUserIds
        }).walksList

    suspend fun getMyActiveWalks(userId: Long): List<WalkInfo> =
        stub.getMyActiveWalks(getMyActiveWalksRequest {
            this.userId = userId
        }).walksList

    suspend fun getWalkGridCell(walkId: Long): String =
        stub.getWalkGridCell(getWalkGridCellRequest {
            this.walkId = walkId
        }).gridCell

    suspend fun getNearbyPatterns(gridCell: String, userId: Long, blockedUserIds: List<Long>): List<WalkPatternInfo> =
        stub.getNearbyPatterns(getNearbyPatternsRequest {
            this.gridCell = gridCell
            this.userId = userId
            this.blockedUserIds += blockedUserIds
        }).patternsList
}
