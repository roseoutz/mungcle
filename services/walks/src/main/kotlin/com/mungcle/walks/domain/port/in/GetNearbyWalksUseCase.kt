package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.NearbyWalkInfo

interface GetNearbyWalksUseCase {
    suspend fun execute(query: Query): List<NearbyWalkInfo>

    data class Query(
        val gridCell: String,
        val userId: Long,
        val blockedUserIds: List<Long>,
    )
}
