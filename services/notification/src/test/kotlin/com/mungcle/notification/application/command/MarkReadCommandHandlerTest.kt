package com.mungcle.notification.application.command

import com.mungcle.notification.domain.exception.NotificationNotFoundException
import com.mungcle.notification.domain.exception.NotificationNotOwnedException
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class MarkReadCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val handler = MarkReadCommandHandler(notificationRepository)

    @Test
    fun `정상 읽음 처리`() = runTest {
        val notification = Notification(
            id = 1L,
            userId = 100L,
            type = NotificationType.GREETING_RECEIVED,
        )
        coEvery { notificationRepository.findById(1L) } returns notification
        coEvery { notificationRepository.save(any()) } answers { firstArg() }

        handler.execute(notificationId = 1L, userId = 100L)

        coVerify { notificationRepository.save(match { it.isRead() }) }
    }

    @Test
    fun `존재하지 않는 알림은 NotificationNotFoundException`() = runTest {
        coEvery { notificationRepository.findById(999L) } returns null

        assertThrows<NotificationNotFoundException> {
            handler.execute(notificationId = 999L, userId = 100L)
        }
    }

    @Test
    fun `다른 사용자의 알림은 NotificationNotOwnedException`() = runTest {
        val notification = Notification(
            id = 1L,
            userId = 100L,
            type = NotificationType.GREETING_RECEIVED,
        )
        coEvery { notificationRepository.findById(1L) } returns notification

        assertThrows<NotificationNotOwnedException> {
            handler.execute(notificationId = 1L, userId = 200L)
        }
    }

    @Test
    fun `이미 읽은 알림은 저장하지 않음`() = runTest {
        val notification = Notification(
            id = 1L,
            userId = 100L,
            type = NotificationType.GREETING_RECEIVED,
            readAt = Instant.now(),
        )
        coEvery { notificationRepository.findById(1L) } returns notification

        handler.execute(notificationId = 1L, userId = 100L)

        coVerify(exactly = 0) { notificationRepository.save(any()) }
    }
}
