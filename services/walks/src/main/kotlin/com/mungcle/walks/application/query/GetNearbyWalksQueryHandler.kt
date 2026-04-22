package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.NearbyWalkInfo
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.out.IdentityPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetNearbyWalksQueryHandler(
    private val walkRepository: WalkRepositoryPort,
    private val identityPort: IdentityPort,
) : GetNearbyWalksUseCase {

    override suspend fun execute(query: GetNearbyWalksUseCase.Query): List<NearbyWalkInfo> {
        val centerCell = GridCell(query.gridCell)
        val adjacentCells = GridCell.adjacentCells(centerCell)

        // identity 서비스에서 차단 목록 직접 조회 + request의 것도 합침 (union)
        val identityBlockedIds = identityPort.getBlockedUserIds(query.userId)
        val blockedSet = (query.blockedUserIds + identityBlockedIds).toSet()

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
