package com.mungcle.notification.application.command

import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.out.IdentityPort
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CreateNotificationCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val pushSender: PushSenderPort = mockk()
    private val identityPort: IdentityPort = mockk()
    private val handler = CreateNotificationCommandHandler(notificationRepository, pushSender, identityPort)

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

        coEvery { notificationRepository.saveEventId("evt-1") } returns Unit
        coEvery { notificationRepository.save(any()) } returns savedNotification
        coEvery { identityPort.getPushToken(1L) } returns "fcm-token-abc"
        coEvery { pushSender.sendPush(savedNotification, "fcm-token-abc") } returns Unit

        val result = handler.execute(command)

        assertEquals(42L, result.id)
        assertEquals(NotificationType.GREETING_RECEIVED, result.type)
        assertFalse(result.isRead)
        coVerify { notificationRepository.saveEventId("evt-1") }
        coVerify { notificationRepository.save(any()) }
        coVerify { identityPort.getPushToken(1L) }
        coVerify { pushSender.sendPush(savedNotification, "fcm-token-abc") }
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

        coEvery { notificationRepository.saveEventId("evt-2") } returns Unit
        coEvery { notificationRepository.save(any()) } returns savedNotification
        coEvery { identityPort.getPushToken(1L) } returns "fcm-token-xyz"
        coEvery { pushSender.sendPush(any(), any()) } throws RuntimeException("FCM 연결 실패")

        val result = handler.execute(command)

        assertEquals(43L, result.id)
        coVerify { notificationRepository.save(any()) }
        coVerify { notificationRepository.saveEventId("evt-2") }
    }

    @Test
    fun `중복 eventId는 DataIntegrityViolationException으로 원자적 무시`() = runTest {
        val command = CreateNotificationCommand(
            userId = 1L,
            type = NotificationType.GREETING_ACCEPTED,
            payload = emptyMap(),
            eventId = "evt-dup",
        )

        coEvery { notificationRepository.saveEventId("evt-dup") } throws DataIntegrityViolationException("duplicate key")

        val result = handler.execute(command)

        assertEquals(0L, result.id)
        coVerify(exactly = 0) { notificationRepository.save(any()) }
        coVerify(exactly = 0) { pushSender.sendPush(any(), any()) }
    }

    @Test
    fun `pushToken이 없으면 푸시 발송 생략`() = runTest {
        val command = CreateNotificationCommand(
            userId = 2L,
            type = NotificationType.GREETING_RECEIVED,
            payload = mapOf("greetingId" to "200"),
            eventId = "evt-3",
        )
        val savedNotification = Notification(
            id = 44L,
            userId = 2L,
            type = NotificationType.GREETING_RECEIVED,
            payload = mapOf("greetingId" to "200"),
        )

        coEvery { notificationRepository.saveEventId("evt-3") } returns Unit
        coEvery { notificationRepository.save(any()) } returns savedNotification
        coEvery { identityPort.getPushToken(2L) } returns null

        val result = handler.execute(command)

        assertEquals(44L, result.id)
        coVerify { notificationRepository.save(any()) }
        coVerify(exactly = 0) { pushSender.sendPush(any(), any()) }
    }
}
