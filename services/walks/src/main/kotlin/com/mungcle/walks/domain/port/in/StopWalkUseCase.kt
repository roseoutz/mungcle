package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk

interface StopWalkUseCase {
    suspend fun execute(command: Command): Walk

    data class Command(
        val walkId: Long,
        val userId: Long,
    )
}
