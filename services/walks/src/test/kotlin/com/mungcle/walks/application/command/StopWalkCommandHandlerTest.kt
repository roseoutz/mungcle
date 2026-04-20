package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class StopWalkCommandHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = StopWalkCommandHandler(walkRepository)

    private fun createActiveWalk(userId: Long = 1L) = Walk(
        id = 100L,
        dogId = 10L,
        userId = userId,
        type = WalkType.OPEN,
        gridCell = "100:200",
        status = WalkStatus.ACTIVE,
        startedAt = Instant.now(),
        endsAt = Instant.now().plusSeconds(3600),
    )

    @Test
    fun `정상 산책 종료`() = runTest {
        val walk = createActiveWalk()
        coEvery { walkRepository.findById(100L) } returns walk
        coEvery { walkRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(walkId = 100L, userId = 1L)

        assertEquals(WalkStatus.ENDED, result.status)
        coVerify(exactly = 1) { walkRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 산책 종료 시 예외`() = runTest {
        coEvery { walkRepository.findById(999L) } returns null

        assertThrows<WalkNotFoundException> {
            handler.execute(walkId = 999L, userId = 1L)
        }
    }

    @Test
    fun `타인의 산책 종료 시 예외`() = runTest {
        val walk = createActiveWalk(userId = 1L)
        coEvery { walkRepository.findById(100L) } returns walk

        assertThrows<WalkNotOwnedException> {
            handler.execute(walkId = 100L, userId = 999L)
        }
    }

    @Test
    fun `이미 종료된 산책 종료 시 예외`() = runTest {
        val walk = createActiveWalk().copy(status = WalkStatus.ENDED)
        coEvery { walkRepository.findById(100L) } returns walk

        assertThrows<WalkAlreadyEndedException> {
            handler.execute(walkId = 100L, userId = 1L)
        }
    }
}
