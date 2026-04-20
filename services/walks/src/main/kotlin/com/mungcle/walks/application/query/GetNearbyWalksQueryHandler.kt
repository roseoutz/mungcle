package com.mungcle.walks.application.query

import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetNearbyWalksQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetNearbyWalksUseCase {

    override suspend fun execute(
        gridCell: String,
        userId: Long,
        blockedUserIds: List<Long>,
    ): List<GetNearbyWalksUseCase.Result> {
        val center = GridCell(gridCell)
        val adjacentCells = GridCell.adjacentCells(center)
        val cellValues = adjacentCells.map { it.value }

        val walks = walkRepository.findByGridCellInAndStatus(cellValues, WalkStatus.ACTIVE)

        return walks
            .filter { it.isOpen() }
            .filter { it.userId != userId }
            .filter { it.userId !in blockedUserIds }
            .map { walk ->
                val distance = GridCell.gridDistance(center, GridCell(walk.gridCell))
                GetNearbyWalksUseCase.Result(walk = walk, gridDistance = distance)
            }
            .filter { it.gridDistance <= 2 }
    }
}
