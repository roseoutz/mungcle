package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class StartWalkCommandHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = StartWalkCommandHandler(walkRepository)

    private val command = StartWalkUseCase.Command(
        userId = 1L,
        dogId = 100L,
        type = WalkType.OPEN,
        lat = 37.5665,
        lng = 126.978,
    )

    @Test
    fun `정상 산책 시작`() = runTest {
        coEvery { walkRepository.findActiveByDogId(100L) } returns null
        val walkSlot = slot<Walk>()
        coEvery { walkRepository.save(capture(walkSlot)) } answers {
            walkSlot.captured.copy(id = 1L)
        }

        val result = handler.execute(command)

        assertEquals(1L, result.id)
        assertEquals(100L, result.dogId)
        assertEquals(1L, result.userId)
        assertEquals(WalkType.OPEN, result.type)
        assertEquals(WalkStatus.ACTIVE, result.status)
        coVerify { walkRepository.save(any()) }
    }

    @Test
    fun `산책 시작 시 endsAt은 60분 후`() = runTest {
        coEvery { walkRepository.findActiveByDogId(100L) } returns null
        val walkSlot = slot<Walk>()
        coEvery { walkRepository.save(capture(walkSlot)) } answers {
            walkSlot.captured.copy(id = 1L)
        }

        handler.execute(command)

        val saved = walkSlot.captured
        val diff = Duration.between(saved.startedAt, saved.endsAt)
        assertEquals(60, diff.toMinutes())
    }

    @Test
    fun `GPS 좌표가 GridCell로 변환되어 저장`() = runTest {
        coEvery { walkRepository.findActiveByDogId(100L) } returns null
        val walkSlot = slot<Walk>()
        coEvery { walkRepository.save(capture(walkSlot)) } answers {
            walkSlot.captured.copy(id = 1L)
        }

        handler.execute(command)

        val expected = GridCell.fromCoordinates(37.5665, 126.978)
        assertEquals(expected, walkSlot.captured.gridCell)
    }

    @Test
    fun `이미 활성 산책이 있으면 WalkAlreadyActiveException`() = runTest {
        val existingWalk = Walk(
            id = 99L,
            dogId = 100L,
            userId = 1L,
            type = WalkType.OPEN,
            gridCell = GridCell("10:20"),
            status = WalkStatus.ACTIVE,
            startedAt = Instant.now(),
            endsAt = Instant.now().plus(Duration.ofMinutes(60)),
        )
        coEvery { walkRepository.findActiveByDogId(100L) } returns existingWalk

        assertThrows<WalkAlreadyActiveException> {
            handler.execute(command)
        }
    }
}
