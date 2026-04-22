package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.GetNearbyWalksUseCase
import com.mungcle.walks.domain.port.out.IdentityPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetNearbyWalksQueryHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val identityPort: IdentityPort = mockk()
    private val handler = GetNearbyWalksQueryHandler(walkRepository, identityPort)

    private val now = Instant.now()

    private fun createWalk(
        id: Long,
        userId: Long,
        dogId: Long = id * 10,
        type: WalkType = WalkType.OPEN,
        gridCell: String = "10:20",
    ) = Walk(
        id = id,
        dogId = dogId,
        userId = userId,
        type = type,
        gridCell = GridCell(gridCell),
        status = WalkStatus.ACTIVE,
        startedAt = now,
        endsAt = now.plus(Duration.ofMinutes(60)),
    )

    @Test
    fun `본인 산책은 제외`() = runTest {
        val myWalk = createWalk(id = 1L, userId = 100L)
        val otherWalk = createWalk(id = 2L, userId = 200L)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(myWalk, otherWalk)
        coEvery { identityPort.getBlockedUserIds(100L) } returns emptyList()

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
        assertEquals(200L, result[0].userId)
    }

    @Test
    fun `차단된 사용자 산책은 제외 - request blockedUserIds`() = runTest {
        val blockedWalk = createWalk(id = 1L, userId = 300L)
        val normalWalk = createWalk(id = 2L, userId = 200L)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(blockedWalk, normalWalk)
        coEvery { identityPort.getBlockedUserIds(100L) } returns emptyList()

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = listOf(300L))
        )

        assertEquals(1, result.size)
        assertEquals(200L, result[0].userId)
    }

    @Test
    fun `차단된 사용자 산책은 제외 - IdentityPort 조회`() = runTest {
        val blockedWalk = createWalk(id = 1L, userId = 300L)
        val normalWalk = createWalk(id = 2L, userId = 200L)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(blockedWalk, normalWalk)
        coEvery { identityPort.getBlockedUserIds(100L) } returns listOf(300L)

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = emptyList())
        )

        assertEquals(1, result.size)
        assertEquals(200L, result[0].userId)
    }

    @Test
    fun `gridDistance가 올바르게 계산됨`() = runTest {
        val nearWalk = createWalk(id = 1L, userId = 200L, gridCell = "10:20")
        val farWalk = createWalk(id = 2L, userId = 300L, gridCell = "11:21")
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(nearWalk, farWalk)
        coEvery { identityPort.getBlockedUserIds(100L) } returns emptyList()

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = emptyList())
        )

        assertEquals(2, result.size)
        val distances = result.associate { it.userId to it.gridDistance }
        assertEquals(0, distances[200L])
        assertEquals(1, distances[300L])
    }

    @Test
    fun `결과가 없으면 빈 리스트`() = runTest {
        every { walkRepository.findActiveOpenByGridCells(any()) } returns emptyList()
        coEvery { identityPort.getBlockedUserIds(100L) } returns emptyList()

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = emptyList())
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `본인과 차단 모두 제외 - request와 IdentityPort 합산`() = runTest {
        val myWalk = createWalk(id = 1L, userId = 100L)
        val blockedWalk = createWalk(id = 2L, userId = 300L)
        val anotherBlockedWalk = createWalk(id = 4L, userId = 400L)
        val normalWalk = createWalk(id = 3L, userId = 200L)
        every { walkRepository.findActiveOpenByGridCells(any()) } returns listOf(myWalk, blockedWalk, anotherBlockedWalk, normalWalk)
        coEvery { identityPort.getBlockedUserIds(100L) } returns listOf(400L)

        val result = handler.execute(
            GetNearbyWalksUseCase.Query(gridCell = "10:20", userId = 100L, blockedUserIds = listOf(300L))
        )

        assertEquals(1, result.size)
        assertEquals(200L, result[0].userId)
    }
}
