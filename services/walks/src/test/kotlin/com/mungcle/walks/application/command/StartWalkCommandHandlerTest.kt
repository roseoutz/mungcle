package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class StartWalkCommandHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = StartWalkCommandHandler(walkRepository)

    private val command = StartWalkUseCase.Command(
        userId = 1L,
        dogId = 10L,
        type = WalkType.OPEN,
        lat = 37.5665,
        lng = 126.978,
    )

    @Test
    fun `정상 산책 시작`() = runTest {
        coEvery { walkRepository.findByDogIdAndStatus(10L, WalkStatus.ACTIVE) } returns null
        val walkSlot = slot<Walk>()
        coEvery { walkRepository.save(capture(walkSlot)) } answers { walkSlot.captured.copy(id = 100L) }

        val result = handler.execute(command)

        assertEquals(100L, result.id)
        assertEquals(10L, result.dogId)
        assertEquals(1L, result.userId)
        assertEquals(WalkType.OPEN, result.type)
        assertEquals(WalkStatus.ACTIVE, result.status)
        coVerify(exactly = 1) { walkRepository.save(any()) }
    }

    @Test
    fun `이미 활성 산책이 있으면 예외`() = runTest {
        val existingWalk = Walk(
            id = 50L,
            dogId = 10L,
            userId = 1L,
            type = WalkType.OPEN,
            gridCell = "100:200",
            status = WalkStatus.ACTIVE,
            startedAt = Instant.now(),
            endsAt = Instant.now().plusSeconds(3600),
        )
        coEvery { walkRepository.findByDogIdAndStatus(10L, WalkStatus.ACTIVE) } returns existingWalk

        assertThrows<WalkAlreadyActiveException> {
            handler.execute(command)
        }
    }

    @Test
    fun `GPS 좌표는 gridCell로 변환되어 저장`() = runTest {
        coEvery { walkRepository.findByDogIdAndStatus(10L, WalkStatus.ACTIVE) } returns null
        val walkSlot = slot<Walk>()
        coEvery { walkRepository.save(capture(walkSlot)) } answers { walkSlot.captured.copy(id = 100L) }

        handler.execute(command)

        val savedWalk = walkSlot.captured
        assert(savedWalk.gridCell.matches(Regex("-?\\d+:-?\\d+")))
    }
}
