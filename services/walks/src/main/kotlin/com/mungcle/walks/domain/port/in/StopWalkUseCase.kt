package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface StopWalkUseCase {
    suspend fun execute(walkId: Long, userId: Long): Walk
}
