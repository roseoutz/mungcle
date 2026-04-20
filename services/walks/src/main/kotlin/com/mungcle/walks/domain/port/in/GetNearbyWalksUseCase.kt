package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface GetNearbyWalksUseCase {
    data class Result(
        val walk: Walk,
        val gridDistance: Int,
    )

    suspend fun execute(gridCell: String, userId: Long, blockedUserIds: List<Long>): List<Result>
}
