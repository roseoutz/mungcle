package com.mungcle.social.application.command

import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.`in`.ExpireGreetingsUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class ExpireGreetingsCommandHandlerTest {

    private val greetingRepository: GreetingRepositoryPort = mockk()
    private val eventPublisher: SocialEventPublisherPort = mockk(relaxed = true)

    private val handler = ExpireGreetingsCommandHandler(
        greetingRepository = greetingRepository,
        eventPublisher = eventPublisher,
    )

    private fun pendingGreeting(id: Long) = Greeting(
        id = id,
        senderUserId = 10L,
        senderDogId = 100L,
        receiverUserId = 20L,
        receiverDogId = 200L,
        receiverWalkId = 300L + id,
        status = GreetingStatus.PENDING,
        expiresAt = Instant.now().minusSeconds(1),
    )

    private fun acceptedGreeting(id: Long) = Greeting(
        id = id,
        senderUserId = 10L,
        senderDogId = 100L,
        receiverUserId = 20L,
        receiverDogId = 200L,
        receiverWalkId = 400L + id,
        status = GreetingStatus.ACCEPTED,
        respondedAt = Instant.now().minusSeconds(1800),
        expiresAt = Instant.now().minusSeconds(1),
    )

    @Test
    fun `PENDING 만료 — 5건 처리되고 이벤트 발행`() {
        val now = Instant.now()
        val pending = (1L..5L).map { pendingGreeting(it) }
        every { greetingRepository.findExpiredPending(now) } returns pending
        every { greetingRepository.findExpiredAccepted(now) } returns emptyList()
        every { greetingRepository.saveAll(any()) } answers { firstArg() }

        val count = handler.execute(now)

        assertEquals(5, count)
        verify(exactly = 5) { eventPublisher.publishGreetingExpired(any()) }
    }

    @Test
    fun `ACCEPTED 만료 — 3건 처리되고 이벤트 발행`() {
        val now = Instant.now()
        val accepted = (10L..12L).map { acceptedGreeting(it) }
        every { greetingRepository.findExpiredPending(now) } returns emptyList()
        every { greetingRepository.findExpiredAccepted(now) } returns accepted
        every { greetingRepository.saveAll(any()) } answers { firstArg() }

        val count = handler.execute(now)

        assertEquals(3, count)
        verify(exactly = 3) { eventPublisher.publishGreetingExpired(any()) }
    }

    @Test
    fun `만료 없음 — 0 반환, 이벤트 미발행`() {
        val now = Instant.now()
        every { greetingRepository.findExpiredPending(now) } returns emptyList()
        every { greetingRepository.findExpiredAccepted(now) } returns emptyList()

        val count = handler.execute(now)

        assertEquals(0, count)
        verify(exactly = 0) { eventPublisher.publishGreetingExpired(any()) }
    }

    @Test
    fun `PENDING + ACCEPTED 혼합 만료 — 이벤트 각각 발행`() {
        val now = Instant.now()
        val pending = listOf(pendingGreeting(1L), pendingGreeting(2L))
        val accepted = listOf(acceptedGreeting(10L))
        every { greetingRepository.findExpiredPending(now) } returns pending
        every { greetingRepository.findExpiredAccepted(now) } returns accepted
        val savedSlot = slot<List<Greeting>>()
        every { greetingRepository.saveAll(capture(savedSlot)) } answers { firstArg() }

        val count = handler.execute(now)

        assertEquals(3, count)
        verify(exactly = 3) { eventPublisher.publishGreetingExpired(any()) }
        assertEquals(3, savedSlot.captured.size)
    }
}
