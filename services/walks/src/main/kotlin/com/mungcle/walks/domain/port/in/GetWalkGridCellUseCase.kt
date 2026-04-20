package com.mungcle.walks.domain.port.`in`

interface GetWalkGridCellUseCase {
    suspend fun execute(walkId: Long): String
}
