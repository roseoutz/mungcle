package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingExpiredException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.`in`.RespondGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class RespondGreetingCommandHandlerTest {

    private val greetingRepository: GreetingRepositoryPort = mockk()
    private val eventPublisher: SocialEventPublisherPort = mockk(relaxed = true)

    private val handler = RespondGreetingCommandHandler(
        greetingRepository = greetingRepository,
        eventPublisher = eventPublisher,
    )

    private fun pendingGreeting(
        id: Long = 1L,
        receiverUserId: Long = 20L,
        expiresAt: Instant = Instant.now().plusSeconds(300),
    ) = Greeting(
        id = id,
        senderUserId = 10L,
        senderDogId = 100L,
        receiverUserId = receiverUserId,
        receiverDogId = 200L,
        receiverWalkId = 300L,
        status = GreetingStatus.PENDING,
        expiresAt = expiresAt,
    )

    @Test
    fun `수락 — ACCEPTED 상태로 전이되고 이벤트 발행`() {
        val greeting = pendingGreeting()
        every { greetingRepository.findById(1L) } returns greeting
        every { greetingRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(RespondGreetingUseCase.Command(1L, 20L, accept = true))

        assertEquals(GreetingStatus.ACCEPTED, result.status)
        verify { eventPublisher.publishGreetingAccepted(any()) }
    }

    @Test
    fun `거절 — EXPIRED 상태로 전이, 이벤트 없음`() {
        val greeting = pendingGreeting()
        every { greetingRepository.findById(1L) } returns greeting
        every { greetingRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(RespondGreetingUseCase.Command(1L, 20L, accept = false))

        assertEquals(GreetingStatus.EXPIRED, result.status)
        verify(exactly = 0) { eventPublisher.publishGreetingAccepted(any()) }
    }

    @Test
    fun `존재하지 않는 인사 → GreetingNotFoundException`() {
        every { greetingRepository.findById(99L) } returns null

        assertThrows<GreetingNotFoundException> {
            handler.execute(RespondGreetingUseCase.Command(99L, 20L, accept = true))
        }
    }

    @Test
    fun `만료된 인사 → GreetingExpiredException`() {
        val expired = pendingGreeting(expiresAt = Instant.now().minusSeconds(1))
        every { greetingRepository.findById(1L) } returns expired

        assertThrows<GreetingExpiredException> {
            handler.execute(RespondGreetingUseCase.Command(1L, 20L, accept = true))
        }
    }

    @Test
    fun `수신자가 아닌 사용자 응답 → GreetingAccessDeniedException`() {
        val greeting = pendingGreeting(receiverUserId = 20L)
        every { greetingRepository.findById(1L) } returns greeting

        assertThrows<GreetingAccessDeniedException> {
            handler.execute(RespondGreetingUseCase.Command(1L, responderUserId = 99L, accept = true))
        }
    }
}
