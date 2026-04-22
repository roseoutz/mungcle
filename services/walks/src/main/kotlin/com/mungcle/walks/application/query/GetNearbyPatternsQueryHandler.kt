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

        // identity 서비스에서 차단 목록 직접 조회 + request의 것도 합침 (union)
        // 패턴은 dogId 기준이므로, 블록된 userId 소유의 dogId를 제외하려면 추가 조회가 필요.
        // 현재는 차단 목록을 수집만 하며, ACTIVE 산책 중 차단된 userId 소유 dogId는 이미 activeWalkDogIds에서 제외됨.
        val identityBlockedIds = identityPort.getBlockedUserIds(query.userId)
        @Suppress("UNUSED_VARIABLE")
        val blockedUserIds = (query.blockedUserIds + identityBlockedIds).toSet()

        val activeWalkDogIds = walkRepository
            .findActiveOpenByGridCells(GridCell.adjacentCells(centerCell))
            .map { it.dogId }
            .toSet()

        return walkPatternRepository
            .findByGridCellsAndHourRange(adjacentCells, hourRange)
            .filter { it.lastWalkedAt.isAfter(cutoff) }
            .filter { it.dogId !in activeWalkDogIds }
            .sortedByDescending { it.walkCount }
            .take(10)
    }
}
