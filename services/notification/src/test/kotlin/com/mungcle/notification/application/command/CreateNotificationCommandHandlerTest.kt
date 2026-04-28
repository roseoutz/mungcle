package com.mungcle.notification.application.command

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushNotificationPort
import com.mungcle.notification.domain.port.out.UserPushTokenPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class CreateNotificationCommandHandlerTest {

    private val notificationRepository: NotificationRepositoryPort = mockk()
    private val pushNotificationPort: PushNotificationPort = mockk()
    private val userPushTokenPort: UserPushTokenPort = mockk()
    private val handler = CreateNotificationCommandHandler(
        notificationRepository,
        pushNotificationPort,
        userPushTokenPort,
    )

    private fun savedNotification(type: NotificationType = NotificationType.GREETING_RECEIVED): Notification =
        Notification(id = 1L, userId = 10L, type = type, payloadJson = "{}", createdAt = Instant.now())

    @Test
    fun `알림 저장 후 FCM push 발송 — pushToken 있을 때`() {
        val notification = savedNotification()
        every { notificationRepository.save(any()) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns "fcm-token-abc"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        handler.execute(CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.GREETING_RECEIVED, payloadJson = "{}"))

        verify {
            pushNotificationPort.send(
                pushToken = "fcm-token-abc",
                title = "새 인사가 왔어요!",
                body = "누군가 산책에 합류하고 싶어해요 🐕",
                data = mapOf("notificationId" to "1", "type" to "GREETING_RECEIVED"),
            )
        }
    }

    @Test
    fun `pushToken 없으면 push 스킵 — 인앱 알림만 저장`() {
        val notification = savedNotification()
        every { notificationRepository.save(any()) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns null

        handler.execute(CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.GREETING_RECEIVED, payloadJson = "{}"))

        verify(exactly = 0) { pushNotificationPort.send(any(), any(), any(), any()) }
    }

    @Test
    fun `GREETING_ACCEPTED 타입 push 제목 검증`() {
        val notification = savedNotification(NotificationType.GREETING_ACCEPTED)
        every { notificationRepository.save(any()) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        handler.execute(CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.GREETING_ACCEPTED, payloadJson = "{}"))

        verify { pushNotificationPort.send(any(), title = "인사가 수락됐어요!", any(), any()) }
    }

    @Test
    fun `MESSAGE_RECEIVED 타입 push 제목 검증`() {
        val notification = savedNotification(NotificationType.MESSAGE_RECEIVED)
        every { notificationRepository.save(any()) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        handler.execute(CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.MESSAGE_RECEIVED, payloadJson = "{}"))

        verify { pushNotificationPort.send(any(), title = "새 메시지가 도착했어요", any(), any()) }
    }

    @Test
    fun `WALK_EXPIRED 타입 push 제목 검증`() {
        val notification = savedNotification(NotificationType.WALK_EXPIRED)
        every { notificationRepository.save(any()) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        handler.execute(CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.WALK_EXPIRED, payloadJson = "{}"))

        verify { pushNotificationPort.send(any(), title = "산책이 종료됐어요", any(), any()) }
    }

    @Test
    fun `알림은 항상 저장 — FCM pushToken 조회 실패해도`() {
        val notification = savedNotification()
        val savedSlot = slot<Notification>()
        every { notificationRepository.save(capture(savedSlot)) } returns notification
        coEvery { userPushTokenPort.getPushToken(10L) } returns null

        val result = handler.execute(
            CreateNotificationUseCase.Command(userId = 10L, type = NotificationType.GREETING_RECEIVED, payloadJson = "{}")
        )

        verify(exactly = 1) { notificationRepository.save(any()) }
        assert(result.id == 1L)
    }
}
