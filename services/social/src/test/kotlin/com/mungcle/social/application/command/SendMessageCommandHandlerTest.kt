package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingNotAcceptedException
import com.mungcle.social.domain.exception.MessageTooLongException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.model.Message
import com.mungcle.social.domain.port.`in`.SendMessageUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.MessageRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class SendMessageCommandHandlerTest {

    private val greetingRepository: GreetingRepositoryPort = mockk()
    private val messageRepository: MessageRepositoryPort = mockk()
    private val eventPublisher: SocialEventPublisherPort = mockk(relaxed = true)

    private val handler = SendMessageCommandHandler(
        greetingRepository = greetingRepository,
        messageRepository = messageRepository,
        eventPublisher = eventPublisher,
    )

    private fun acceptedGreeting(
        id: Long = 1L,
        senderUserId: Long = 10L,
        receiverUserId: Long = 20L,
        expiresAt: Instant = Instant.now().plusSeconds(1800),
    ) = Greeting(
        id = id,
        senderUserId = senderUserId,
        senderDogId = 100L,
        receiverUserId = receiverUserId,
        receiverDogId = 200L,
        receiverWalkId = 300L,
        status = GreetingStatus.ACCEPTED,
        respondedAt = Instant.now().minusSeconds(60),
        expiresAt = expiresAt,
    )

    @Test
    fun `정상 메시지 전송`() {
        val greeting = acceptedGreeting()
        every { greetingRepository.findById(1L) } returns greeting
        val slot = slot<Message>()
        every { messageRepository.save(capture(slot)) } answers { slot.captured.copy(id = 100L) }

        val result = handler.execute(SendMessageUseCase.Command(1L, 10L, "안녕하세요"))

        assertEquals(100L, result.id)
        assertEquals(1L, result.greetingId)
        assertEquals(10L, result.senderUserId)
        assertEquals("안녕하세요", result.body)
        verify { eventPublisher.publishMessageSent(any(), any()) }
    }

    @Test
    fun `140자 초과 메시지 → MessageTooLongException`() {
        val longBody = "a".repeat(141)

        assertThrows<MessageTooLongException> {
            handler.execute(SendMessageUseCase.Command(1L, 10L, longBody))
        }
    }

    @Test
    fun `빈 문자열 메시지 → MessageTooLongException`() {
        assertThrows<MessageTooLongException> {
            handler.execute(SendMessageUseCase.Command(1L, 10L, "   "))
        }
    }

    @Test
    fun `PENDING 상태 인사에 메시지 → GreetingNotAcceptedException`() {
        val pendingGreeting = Greeting(
            id = 1L,
            senderUserId = 10L,
            senderDogId = 100L,
            receiverUserId = 20L,
            receiverDogId = 200L,
            receiverWalkId = 300L,
            status = GreetingStatus.PENDING,
            expiresAt = Instant.now().plusSeconds(300),
        )
        every { greetingRepository.findById(1L) } returns pendingGreeting

        assertThrows<GreetingNotAcceptedException> {
            handler.execute(SendMessageUseCase.Command(1L, 10L, "안녕하세요"))
        }
    }

    @Test
    fun `비참여자가 메시지 전송 → GreetingAccessDeniedException`() {
        val greeting = acceptedGreeting()
        every { greetingRepository.findById(1L) } returns greeting

        assertThrows<GreetingAccessDeniedException> {
            handler.execute(SendMessageUseCase.Command(1L, senderUserId = 99L, "안녕하세요"))
        }
    }
}
