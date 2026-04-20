package com.mungcle.identity.application.query

import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetBlockedUserIdsQueryHandlerTest {

    private val blockRepository: BlockRepositoryPort = mockk()
    private val handler = GetBlockedUserIdsQueryHandler(blockRepository)

    @Test
    fun `양방향 차단된 사용자 ID 목록 반환`() = runTest {
        // userId=1 이 2를 차단하고, 3이 1을 차단한 상황 — 양방향이므로 2, 3 반환
        every { blockRepository.findBlockedUserIds(1L) } returns listOf(2L, 3L)

        val result = handler.execute(1L)

        assertEquals(listOf(2L, 3L), result)
    }

    @Test
    fun `차단 관계 없으면 빈 목록 반환`() = runTest {
        every { blockRepository.findBlockedUserIds(1L) } returns emptyList()

        val result = handler.execute(1L)

        assertTrue(result.isEmpty())
    }
}
