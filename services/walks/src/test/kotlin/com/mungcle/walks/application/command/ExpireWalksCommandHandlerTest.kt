package com.mungcle.walks.application.command

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.out.WalkEventPublisherPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class ExpireWalksCommandHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val walkEventPublisher: WalkEventPublisherPort = mockk(relaxed = true)
    private val handler = ExpireWalksCommandHandler(walkRepository, walkEventPublisher)

    private val now = Instant.now()

    private fun createWalk(id: Long, userId: Long = 1L, endsAt: Instant = now.minus(Duration.ofMinutes(1))) = Walk(
        id = id,
        dogId = id * 10,
        userId = userId,
        type = WalkType.OPEN,
        gridCell = GridCell("10:20"),
        status = WalkStatus.ACTIVE,
        startedAt = now.minus(Duration.ofMinutes(70)),
        endsAt = endsAt,
    )

    @Test
    fun `만료 대상 산책을 찾아 end 처리 후 저장`() {
        val walk = createWalk(id = 1L)
        every { walkRepository.findExpiredActive(now) } returns listOf(walk)
        val savedSlot = slot<Walk>()
        every { walkRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        handler.execute(now)

        val saved = savedSlot.captured
        assertEquals(WalkStatus.ENDED, saved.status)
        assertEquals(now, saved.endsAt)
    }

    @Test
    fun `만료된 각 산책에 대해 WalkExpiredEvent 발행`() {
        val walk1 = createWalk(id = 1L, userId = 100L)
        val walk2 = createWalk(id = 2L, userId = 200L)
        every { walkRepository.findExpiredActive(now) } returns listOf(walk1, walk2)
        every { walkRepository.save(any()) } answers { firstArg() }

        handler.execute(now)

        verify(exactly = 1) { walkEventPublisher.publishWalkExpired(1L, 100L) }
        verify(exactly = 1) { walkEventPublisher.publishWalkExpired(2L, 200L) }
    }

    @Test
    fun `만료된 산책 수 반환`() {
        val walks = listOf(createWalk(id = 1L), createWalk(id = 2L), createWalk(id = 3L))
        every { walkRepository.findExpiredActive(now) } returns walks
        every { walkRepository.save(any()) } answers { firstArg() }

        val count = handler.execute(now)

        assertEquals(3, count)
    }

    @Test
    fun `만료 대상이 없으면 0 반환`() {
        every { walkRepository.findExpiredActive(now) } returns emptyList()

        val count = handler.execute(now)

        assertEquals(0, count)
        verify(exactly = 0) { walkEventPublisher.publishWalkExpired(any(), any()) }
    }
}
