package com.mungcle.notification.application.command

import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CreateNotificationCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val pushSender: PushSenderPort = mockk()
    private val handler = CreateNotificationCommandHandler(notificationRepository, pushSender)

    @Test
    fun `정상 알림 생성 및 푸시 발송`() = runTest {
        val command = CreateNotificationCommand(
            userId = 1L,
            type = NotificationType.GREETING_RECEIVED,
            payload = mapOf("greetingId" to "100"),
            eventId = "evt-1",
        )
        val savedNotification = Notification(
            id = 42L,
            userId = 1L,
            type = NotificationType.GREETING_RECEIVED,
            payload = mapOf("greetingId" to "100"),
        )

        coEvery { notificationRepository.existsByEventId("evt-1") } returns false
        coEvery { notificationRepository.save(any()) } returns savedNotification
        coEvery { notificationRepository.saveEventId("evt-1") } returns Unit
        coEvery { pushSender.sendPush(savedNotification) } returns Unit

        val result = handler.execute(command)

        assertEquals(42L, result.id)
        assertEquals(NotificationType.GREETING_RECEIVED, result.type)
        assertFalse(result.isRead)
        coVerify { notificationRepository.save(any()) }
        coVerify { notificationRepository.saveEventId("evt-1") }
        coVerify { pushSender.sendPush(savedNotification) }
    }

    @Test
    fun `FCM 실패해도 DB 저장 성공`() = runTest {
        val command = CreateNotificationCommand(
            userId = 1L,
            type = NotificationType.MESSAGE_RECEIVED,
            payload = emptyMap(),
            eventId = "evt-2",
        )
        val savedNotification = Notification(
            id = 43L,
            userId = 1L,
            type = NotificationType.MESSAGE_RECEIVED,
        )

        coEvery { notificationRepository.existsByEventId("evt-2") } returns false
        coEvery { notificationRepository.save(any()) } returns savedNotification
        coEvery { notificationRepository.saveEventId("evt-2") } returns Unit
        coEvery { pushSender.sendPush(any()) } throws RuntimeException("FCM 연결 실패")

        val result = handler.execute(command)

        assertEquals(43L, result.id)
        coVerify { notificationRepository.save(any()) }
        coVerify { notificationRepository.saveEventId("evt-2") }
    }

    @Test
    fun `이미 처리된 eventId는 중복 저장하지 않음`() = runTest {
        val command = CreateNotificationCommand(
            userId = 1L,
            type = NotificationType.GREETING_ACCEPTED,
            payload = emptyMap(),
            eventId = "evt-dup",
        )

        coEvery { notificationRepository.existsByEventId("evt-dup") } returns true

        handler.execute(command)

        coVerify(exactly = 0) { notificationRepository.save(any()) }
        coVerify(exactly = 0) { pushSender.sendPush(any()) }
    }
}
