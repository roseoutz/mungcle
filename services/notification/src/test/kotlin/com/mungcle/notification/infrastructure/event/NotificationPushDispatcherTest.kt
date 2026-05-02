package com.mungcle.notification.infrastructure.event

import com.mungcle.notification.domain.event.NotificationCreatedEvent
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.out.PushNotificationPort
import com.mungcle.notification.domain.port.out.UserPushTokenPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class NotificationPushDispatcherTest {

    private val userPushTokenPort: UserPushTokenPort = mockk()
    private val pushNotificationPort: PushNotificationPort = mockk()
    private val dispatcher = NotificationPushDispatcher(userPushTokenPort, pushNotificationPort)

    private fun notification(type: NotificationType = NotificationType.GREETING_RECEIVED): Notification =
        Notification(id = 1L, userId = 10L, type = type, payloadJson = "{}", createdAt = Instant.now())

    @Test
    fun `이벤트 수신 시 pushToken 조회 후 FCM push 발송`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns "fcm-token-abc"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification()))

        verify(exactly = 1) {
            pushNotificationPort.send(
                pushToken = "fcm-token-abc",
                title = "새 인사가 왔어요!",
                body = "누군가 산책에 합류하고 싶어해요 🐕",
                data = mapOf("notificationId" to "1", "type" to "GREETING_RECEIVED"),
            )
        }
    }

    @Test
    fun `pushToken 없으면 push 스킵`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns null

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification()))

        verify(exactly = 0) { pushNotificationPort.send(any(), any(), any(), any()) }
    }

    @Test
    fun `GREETING_ACCEPTED 타입 push 제목 검증`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification(NotificationType.GREETING_ACCEPTED)))

        verify { pushNotificationPort.send(any(), title = "인사가 수락됐어요!", any(), any()) }
    }

    @Test
    fun `MESSAGE_RECEIVED 타입 push 제목 검증`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification(NotificationType.MESSAGE_RECEIVED)))

        verify { pushNotificationPort.send(any(), title = "새 메시지가 도착했어요", any(), any()) }
    }

    @Test
    fun `WALK_EXPIRED 타입 push 제목 검증`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        justRun { pushNotificationPort.send(any(), any(), any(), any()) }

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification(NotificationType.WALK_EXPIRED)))

        verify { pushNotificationPort.send(any(), title = "산책이 종료됐어요", any(), any()) }
    }

    @Test
    fun `pushToken 조회 실패 시 예외를 삼키고 push 발송하지 않음`() {
        coEvery { userPushTokenPort.getPushToken(10L) } throws RuntimeException("identity 서비스 다운")

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification()))

        verify(exactly = 0) { pushNotificationPort.send(any(), any(), any(), any()) }
    }

    @Test
    fun `FCM 발송 실패 시 예외를 삼킴 — 인앱 알림은 정상 저장됨`() {
        coEvery { userPushTokenPort.getPushToken(10L) } returns "token"
        every { pushNotificationPort.send(any(), any(), any(), any()) } throws RuntimeException("FCM 다운")

        dispatcher.onNotificationCreated(NotificationCreatedEvent(notification()))
    }
}
