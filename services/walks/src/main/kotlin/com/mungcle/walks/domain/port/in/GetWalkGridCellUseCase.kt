package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.GridCell

interface GetWalkGridCellUseCase {
    suspend fun execute(walkId: Long): GridCell
}
