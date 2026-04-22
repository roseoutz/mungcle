package com.mungcle.notification.application.command

import com.mungcle.notification.domain.exception.NotificationNotFoundException
import com.mungcle.notification.domain.exception.NotificationNotOwnedException
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.MarkReadUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class MarkReadCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk(relaxed = true)
    private val handler = MarkReadCommandHandler(notificationRepository)

    private fun makeNotification(id: Long, userId: Long): Notification = Notification(
        id = id,
        userId = userId,
        type = NotificationType.GREETING_RECEIVED,
        payloadJson = "{}",
        read = false,
        createdAt = Instant.now(),
    )

    @Test
    fun `정상 읽음 처리`() {
        val notification = makeNotification(id = 1L, userId = 10L)
        every { notificationRepository.findById(1L) } returns notification

        handler.execute(MarkReadUseCase.Command(notificationId = 1L, userId = 10L))

        verify { notificationRepository.markRead(1L) }
    }

    @Test
    fun `알림 없으면 NotificationNotFoundException`() {
        every { notificationRepository.findById(99L) } returns null

        assertThrows<NotificationNotFoundException> {
            handler.execute(MarkReadUseCase.Command(notificationId = 99L, userId = 10L))
        }
    }

    @Test
    fun `다른 사용자의 알림이면 NotificationNotOwnedException`() {
        val notification = makeNotification(id = 1L, userId = 10L)
        every { notificationRepository.findById(1L) } returns notification

        assertThrows<NotificationNotOwnedException> {
            handler.execute(MarkReadUseCase.Command(notificationId = 1L, userId = 99L))
        }
    }
}
