package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface GetMyActiveWalksUseCase {
    suspend fun execute(userId: Long): List<Walk>
}
