package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface GetMyActiveWalksUseCase {
    fun execute(userId: Long): List<Walk>
}
