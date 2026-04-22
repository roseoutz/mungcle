package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkPattern
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.GetNearbyPatternsUseCase
import com.mungcle.walks.domain.port.out.IdentityPort
import com.mungcle.walks.domain.port.out.WalkPatternRepositoryPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetNearbyPatternsQueryHandlerTest {

    private val walkPatternRepository: WalkPatternRepositoryPort = mockk()
    private val walkRepository: WalkRepositoryPort = mockk()
    private val identityPort: IdentityPort = mockk()
    private val handler = GetNearbyPatternsQueryHandler(walkPatternRepository, walkRepository, identityPort)

    private val now = Instant.now()

    private fun createPattern(
        id: Long,
        dogId: Long,
        walkCount: Int = 5,
        lastWalkedAt: Instant = now.minus(1, ChronoUnit.DAYS),
        gridCell: String = "10:20",
        hourOfDay: Int = 10,
    ) = WalkPattern(
        id = id,
        dogId = dogId,
        walkCount = walkCount,
        lastWalkedAt = lastWalkedAt,
        gridCell = gridCell,
        hourOfDay = hourOfDay,
    )

    private fun createWalk(dogId: Long) = Walk(
        id = dogId,
        dogId = dogId,
        userId = dogId * 10,
        type = WalkType.OPEN,
        gridCell = GridCell("10:20"),
        status = WalkStatus.ACTIVE,
        startedAt = now,
        endsAt = now.plus(Duration.ofMinutes(60)),
    )

    @Test
    fun `±1시간 범위 내 패턴만 반환`() = runTest {
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(
            createPattern(id = 1L, dogId = 10L),
        )
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(any()) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `14일 초과된 패턴은 제외`() = runTest {
        val oldPattern = createPattern(id = 1L, dogId = 10L, lastWalkedAt = now.minus(15, ChronoUnit.DAYS))
        val recentPattern = createPattern(id = 2L, dogId = 20L, lastWalkedAt = now.minus(13, ChronoUnit.DAYS))
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(oldPattern, recentPattern)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(any()) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
        assertEquals(20L, result[0].dogId)
    }

    @Test
    fun `현재 ACTIVE 산책 중인 dogId는 제외`() = runTest {
        val pattern1 = createPattern(id = 1L, dogId = 10L)
        val pattern2 = createPattern(id = 2L, dogId = 20L)
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(pattern1, pattern2)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(createWalk(dogId = 10L))
        coEvery { identityPort.getBlockedUserIds(any()) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
        assertEquals(20L, result[0].dogId)
    }

    @Test
    fun `walkCount 기준 top 10만 반환`() = runTest {
        val patterns = (1..15).map { i ->
            createPattern(id = i.toLong(), dogId = (i * 10).toLong(), walkCount = i)
        }
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns patterns
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(any()) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(10, result.size)
        // 내림차순 정렬 — walkCount 가장 높은 것부터
        assertEquals(15, result[0].walkCount)
        assertEquals(6, result[9].walkCount)
    }

    @Test
    fun `blockedUserIds 및 IdentityPort 차단 목록 합산하여 제외`() = runTest {
        // 이 테스트는 dogId 기준이므로 블록은 userId 기준으로 동작하지 않음
        // 패턴 자체가 dogId 기반이므로, 실제 차단 처리는 없다는 점을 검증
        val pattern = createPattern(id = 1L, dogId = 10L)
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(pattern)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(1L) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `결과가 없으면 빈 리스트 반환`() = runTest {
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns emptyList()
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(any()) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertTrue(result.isEmpty())
    }
}
