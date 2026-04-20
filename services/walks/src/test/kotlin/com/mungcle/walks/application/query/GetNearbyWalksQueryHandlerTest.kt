package com.mungcle.walks.application.query

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class GetNearbyWalksQueryHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = GetNearbyWalksQueryHandler(walkRepository)

    private fun createWalk(
        id: Long = 1L,
        userId: Long = 100L,
        dogId: Long = 10L,
        type: WalkType = WalkType.OPEN,
        gridCell: String = "100:200",
    ) = Walk(
        id = id,
        dogId = dogId,
        userId = userId,
        type = type,
        gridCell = gridCell,
        status = WalkStatus.ACTIVE,
        startedAt = Instant.now(),
        endsAt = Instant.now().plusSeconds(3600),
    )

    @Test
    fun `OPEN 산책만 반환`() = runTest {
        val openWalk = createWalk(id = 1L, type = WalkType.OPEN, userId = 200L)
        val soloWalk = createWalk(id = 2L, type = WalkType.SOLO, userId = 300L)
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns listOf(openWalk, soloWalk)

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = emptyList())

        assertEquals(1, results.size)
        assertEquals(WalkType.OPEN, results[0].walk.type)
    }

    @Test
    fun `본인 산책 제외`() = runTest {
        val myWalk = createWalk(id = 1L, userId = 1L)
        val otherWalk = createWalk(id = 2L, userId = 200L)
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns listOf(myWalk, otherWalk)

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = emptyList())

        assertEquals(1, results.size)
        assertEquals(200L, results[0].walk.userId)
    }

    @Test
    fun `차단 사용자 제외`() = runTest {
        val blockedWalk = createWalk(id = 1L, userId = 200L)
        val normalWalk = createWalk(id = 2L, userId = 300L)
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns listOf(blockedWalk, normalWalk)

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = listOf(200L))

        assertEquals(1, results.size)
        assertEquals(300L, results[0].walk.userId)
    }

    @Test
    fun `gridDistance 2 이하만 반환`() = runTest {
        val nearWalk = createWalk(id = 1L, userId = 200L, gridCell = "101:201") // distance 1
        val farWalk = createWalk(id = 2L, userId = 300L, gridCell = "103:203") // distance 3
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns listOf(nearWalk, farWalk)

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = emptyList())

        assertEquals(1, results.size)
        assertEquals(1, results[0].gridDistance)
    }

    @Test
    fun `결과 없으면 빈 리스트 반환`() = runTest {
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns emptyList()

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = emptyList())

        assertTrue(results.isEmpty())
    }

    @Test
    fun `gridDistance 계산 정확성`() = runTest {
        val sameCell = createWalk(id = 1L, userId = 200L, gridCell = "100:200") // distance 0
        val adjacentCell = createWalk(id = 2L, userId = 300L, gridCell = "101:200") // distance 1
        val diagonalCell = createWalk(id = 3L, userId = 400L, gridCell = "102:202") // distance 2
        coEvery { walkRepository.findByGridCellInAndStatus(any(), WalkStatus.ACTIVE) } returns
            listOf(sameCell, adjacentCell, diagonalCell)

        val results = handler.execute(gridCell = "100:200", userId = 1L, blockedUserIds = emptyList())

        assertEquals(3, results.size)
        val distanceMap = results.associate { it.walk.id to it.gridDistance }
        assertEquals(0, distanceMap[1L])
        assertEquals(1, distanceMap[2L])
        assertEquals(2, distanceMap[3L])
    }
}
