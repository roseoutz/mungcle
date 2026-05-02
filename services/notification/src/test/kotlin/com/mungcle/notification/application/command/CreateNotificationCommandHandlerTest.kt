package com.mungcle.notification.application.command

import com.mungcle.notification.domain.event.NotificationCreatedEvent
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import kotlin.test.assertEquals

class CreateNotificationCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk()
    private val handler = CreateNotificationCommandHandler(
        notificationRepository,
        eventPublisher,
    )

    private fun savedNotification(type: NotificationType = NotificationType.GREETING_RECEIVED): Notification =
        Notification(id = 1L, userId = 10L, type = type, payloadJson = "{}", createdAt = Instant.now())

    @Test
    fun `알림 저장 후 NotificationCreatedEvent 발행`() {
        val notification = savedNotification()
        every { notificationRepository.save(any()) } returns notification
        justRun { eventPublisher.publishEvent(any<NotificationCreatedEvent>()) }

        handler.execute(
            CreateNotificationUseCase.Command(
                userId = 10L,
                type = NotificationType.GREETING_RECEIVED,
                payloadJson = "{}",
            )
        )

        verify(exactly = 1) {
            eventPublisher.publishEvent(NotificationCreatedEvent(notification))
        }
    }

    @Test
    fun `알림은 저장 후 반환된다`() {
        val notification = savedNotification()
        every { notificationRepository.save(any()) } returns notification
        justRun { eventPublisher.publishEvent(any<NotificationCreatedEvent>()) }

        val result = handler.execute(
            CreateNotificationUseCase.Command(
                userId = 10L,
                type = NotificationType.GREETING_RECEIVED,
                payloadJson = "{}",
            )
        )

        assertEquals(1L, result.id)
    }

    @Test
    fun `Command의 필드가 Notification에 그대로 매핑된다`() {
        val notification = savedNotification(NotificationType.WALK_EXPIRED)
        val savedSlot = slot<Notification>()
        every { notificationRepository.save(capture(savedSlot)) } returns notification
        justRun { eventPublisher.publishEvent(any<NotificationCreatedEvent>()) }

        handler.execute(
            CreateNotificationUseCase.Command(
                userId = 42L,
                type = NotificationType.WALK_EXPIRED,
                payloadJson = """{"walkId":99}""",
            )
        )

        assertEquals(42L, savedSlot.captured.userId)
        assertEquals(NotificationType.WALK_EXPIRED, savedSlot.captured.type)
        assertEquals("""{"walkId":99}""", savedSlot.captured.payloadJson)
    }
}
