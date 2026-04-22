package com.mungcle.walks.domain.port.`in`

import com.mungcle.common.domain.GridCell

interface GetWalkGridCellUseCase {
    fun execute(walkId: Long): GridCell
}
