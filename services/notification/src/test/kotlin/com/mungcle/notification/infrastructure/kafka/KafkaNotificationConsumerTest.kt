package com.mungcle.notification.infrastructure.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mungcle.common.kafka.GreetingAcceptedEvent
import com.mungcle.common.kafka.GreetingCreatedEvent
import com.mungcle.common.kafka.MessageSentEvent
import com.mungcle.common.kafka.WalkExpiredEvent
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class KafkaNotificationConsumerTest {

    private val createNotificationUseCase: CreateNotificationUseCase = mockk()
    private val objectMapper = jacksonObjectMapper()
    private val consumer = KafkaNotificationConsumer(createNotificationUseCase, objectMapper)

    private fun stubSave(type: NotificationType, userId: Long): Notification {
        val notification = Notification(id = 1L, userId = userId, type = type, payloadJson = "{}", createdAt = Instant.now())
        every { createNotificationUseCase.execute(any()) } returns notification
        return notification
    }

    @Test
    fun `greeting created 이벤트 수신 — GREETING_RECEIVED 알림 생성`() {
        val event = GreetingCreatedEvent(greetingId = 100L, senderUserId = 1L, receiverUserId = 2L)
        stubSave(NotificationType.GREETING_RECEIVED, 2L)
        val commandSlot = slot<CreateNotificationUseCase.Command>()
        every { createNotificationUseCase.execute(capture(commandSlot)) } returns mockk(relaxed = true)

        consumer.onGreetingCreated(event)

        assertEquals(NotificationType.GREETING_RECEIVED, commandSlot.captured.type)
        assertEquals(2L, commandSlot.captured.userId)
    }

    @Test
    fun `greeting accepted 이벤트 수신 — GREETING_ACCEPTED 알림 생성`() {
        val event = GreetingAcceptedEvent(greetingId = 200L, senderUserId = 3L, receiverUserId = 4L)
        val commandSlot = slot<CreateNotificationUseCase.Command>()
        every { createNotificationUseCase.execute(capture(commandSlot)) } returns mockk(relaxed = true)

        consumer.onGreetingAccepted(event)

        assertEquals(NotificationType.GREETING_ACCEPTED, commandSlot.captured.type)
        assertEquals(3L, commandSlot.captured.userId)
    }

    @Test
    fun `message sent 이벤트 수신 — MESSAGE_RECEIVED 알림 생성`() {
        val event = MessageSentEvent(messageId = 300L, greetingId = 10L, senderUserId = 5L, receiverUserId = 6L)
        val commandSlot = slot<CreateNotificationUseCase.Command>()
        every { createNotificationUseCase.execute(capture(commandSlot)) } returns mockk(relaxed = true)

        consumer.onMessageSent(event)

        assertEquals(NotificationType.MESSAGE_RECEIVED, commandSlot.captured.type)
        assertEquals(6L, commandSlot.captured.userId)
    }

    @Test
    fun `walk expired 이벤트 수신 — WALK_EXPIRED 알림 생성`() {
        val event = WalkExpiredEvent(walkId = 400L, userId = 7L)
        val commandSlot = slot<CreateNotificationUseCase.Command>()
        every { createNotificationUseCase.execute(capture(commandSlot)) } returns mockk(relaxed = true)

        consumer.onWalkExpired(event)

        assertEquals(NotificationType.WALK_EXPIRED, commandSlot.captured.type)
        assertEquals(7L, commandSlot.captured.userId)
    }

    @Test
    fun `greeting created — createNotification UseCase 1회 호출 검증`() {
        val event = GreetingCreatedEvent(greetingId = 1L, senderUserId = 10L, receiverUserId = 20L)
        every { createNotificationUseCase.execute(any()) } returns mockk(relaxed = true)

        consumer.onGreetingCreated(event)

        verify(exactly = 1) { createNotificationUseCase.execute(any()) }
    }
}
