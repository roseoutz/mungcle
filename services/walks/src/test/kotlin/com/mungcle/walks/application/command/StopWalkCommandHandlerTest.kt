package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.StopWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class StopWalkCommandHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = StopWalkCommandHandler(walkRepository)

    private val now = Instant.now()
    private val activeWalk = Walk(
        id = 1L,
        dogId = 100L,
        userId = 10L,
        type = WalkType.OPEN,
        gridCell = GridCell("10:20"),
        status = WalkStatus.ACTIVE,
        startedAt = now,
        endsAt = now.plus(Duration.ofMinutes(60)),
    )

    @Test
    fun `정상 산책 종료`() = runTest {
        coEvery { walkRepository.findById(1L) } returns activeWalk
        coEvery { walkRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(StopWalkUseCase.Command(walkId = 1L, userId = 10L))

        assertEquals(WalkStatus.ENDED, result.status)
        coVerify { walkRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 산책은 WalkNotFoundException`() = runTest {
        coEvery { walkRepository.findById(999L) } returns null

        assertThrows<WalkNotFoundException> {
            handler.execute(StopWalkUseCase.Command(walkId = 999L, userId = 10L))
        }
    }

    @Test
    fun `타인의 산책 종료 시 WalkNotOwnedException`() = runTest {
        coEvery { walkRepository.findById(1L) } returns activeWalk

        assertThrows<WalkNotOwnedException> {
            handler.execute(StopWalkUseCase.Command(walkId = 1L, userId = 999L))
        }
    }

    @Test
    fun `이미 종료된 산책은 WalkAlreadyEndedException`() = runTest {
        val endedWalk = activeWalk.copy(status = WalkStatus.ENDED)
        coEvery { walkRepository.findById(1L) } returns endedWalk

        assertThrows<WalkAlreadyEndedException> {
            handler.execute(StopWalkUseCase.Command(walkId = 1L, userId = 10L))
        }
    }
}
