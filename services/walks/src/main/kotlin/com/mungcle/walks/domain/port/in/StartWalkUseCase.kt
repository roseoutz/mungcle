package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkType

interface StartWalkUseCase {
    suspend fun execute(command: Command): Walk

    data class Command(
        val userId: Long,
        val dogId: Long,
        val type: WalkType,
        val lat: Double,
        val lng: Double,
    )
}
