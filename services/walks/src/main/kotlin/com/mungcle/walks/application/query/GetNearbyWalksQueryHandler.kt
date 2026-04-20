package com.mungcle.walks.application.query

import com.mungcle.walks.application.dto.NearbyWalkInfo
import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetNearbyWalksQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetNearbyWalksUseCase {

    override suspend fun execute(query: GetNearbyWalksUseCase.Query): List<NearbyWalkInfo> {
        val centerCell = GridCell(query.gridCell)
        val adjacentCells = GridCell.adjacentCells(centerCell)
        val blockedSet = query.blockedUserIds.toSet()

        val walks = walkRepository.findActiveOpenByGridCells(adjacentCells)

        return walks
            .filter { it.userId != query.userId }
            .filter { it.userId !in blockedSet }
            .map { walk ->
                NearbyWalkInfo(
                    walkId = walk.id,
                    dogId = walk.dogId,
                    userId = walk.userId,
                    gridDistance = GridCell.gridDistance(centerCell, walk.gridCell),
                    startedAt = walk.startedAt,
                )
            }
    }
}
