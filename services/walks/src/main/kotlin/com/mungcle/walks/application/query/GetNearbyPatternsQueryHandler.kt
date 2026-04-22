package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.WalkPattern
import com.mungcle.walks.domain.port.`in`.GetNearbyPatternsUseCase
import com.mungcle.walks.domain.port.out.IdentityPort
import com.mungcle.walks.domain.port.out.WalkPatternRepositoryPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class GetNearbyPatternsQueryHandler(
    private val walkPatternRepository: WalkPatternRepositoryPort,
    private val walkRepository: WalkRepositoryPort,
    private val identityPort: IdentityPort,
) : GetNearbyPatternsUseCase {

    override suspend fun execute(query: GetNearbyPatternsUseCase.Query): List<WalkPattern> {
        val centerCell = GridCell(query.gridCell)
        val adjacentCells = GridCell.adjacentCells(centerCell).map { it.value }

        val now = Instant.now()
        val currentHour = now.atZone(ZoneId.of("Asia/Seoul")).hour
        val hourRange = ((currentHour - 1).coerceAtLeast(0))..((currentHour + 1).coerceAtMost(23))

        val cutoff = now.minus(14, ChronoUnit.DAYS)

        val identityBlockedIds = identityPort.getBlockedUserIds(query.userId)
        val allBlockedUserIds = (query.blockedUserIds + identityBlockedIds).distinct()

        val blockedDogIds = if (allBlockedUserIds.isNotEmpty()) {
            walkRepository.findDogIdsByUserIds(allBlockedUserIds)
        } else {
            emptyList()
        }

        val activeDogIds = walkRepository
            .findActiveOpenByGridCells(GridCell.adjacentCells(centerCell))
            .map { it.dogId }
            .toSet()

        return walkPatternRepository
            .findByGridCellsAndHourRange(adjacentCells, hourRange)
            .filter { it.lastWalkedAt.isAfter(cutoff) }
            .filter { it.dogId !in activeDogIds }
            .filter { it.dogId !in blockedDogIds }
            .sortedByDescending { it.walkCount }
            .take(10)
    }
}
