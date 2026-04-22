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

    private fun createWalk(dogId: Long, userId: Long = dogId * 10) = Walk(
        id = dogId,
        dogId = dogId,
        userId = userId,
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
        every { walkRepository.findDogIdsByUserIds(any()) } returns emptyList()
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
        every { walkRepository.findDogIdsByUserIds(any()) } returns emptyList()
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
        every { walkRepository.findDogIdsByUserIds(any()) } returns emptyList()
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
        every { walkRepository.findDogIdsByUserIds(any()) } returns emptyList()
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
    fun `차단된 userId 소유 dogId는 패턴 결과에서 제외`() = runTest {
        val pattern1 = createPattern(id = 1L, dogId = 10L) // blockedUser(userId=50) 소유
        val pattern2 = createPattern(id = 2L, dogId = 20L) // 차단되지 않은 사용자
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(pattern1, pattern2)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        // blockedUserIds=[50] → dogId=[10] 매핑
        every { walkRepository.findDogIdsByUserIds(listOf(50L)) } returns listOf(10L)
        coEvery { identityPort.getBlockedUserIds(1L) } returns emptyList()

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = listOf(50L))
        )

        assertEquals(1, result.size)
        assertEquals(20L, result[0].dogId)
    }

    @Test
    fun `IdentityPort 차단 목록의 userId 소유 dogId도 제외`() = runTest {
        val pattern1 = createPattern(id = 1L, dogId = 10L) // identityBlocked(userId=99) 소유
        val pattern2 = createPattern(id = 2L, dogId = 20L)
        every { walkPatternRepository.findByGridCellsAndHourRange(any(), any()) } returns listOf(pattern1, pattern2)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        // identityPort가 userId=99를 차단 목록으로 반환 → dogId=10 매핑
        every { walkRepository.findDogIdsByUserIds(listOf(99L)) } returns listOf(10L)
        coEvery { identityPort.getBlockedUserIds(1L) } returns listOf(99L)

        val result = handler.execute(
            GetNearbyPatternsUseCase.Query(gridCell = "10:20", userId = 1L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
        assertEquals(20L, result[0].dogId)
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
