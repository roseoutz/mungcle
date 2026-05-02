package com.mungcle.notification.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class FcmPushClientTest {

    private val firebaseMessaging: FirebaseMessaging = mockk()
    private val client = FcmPushClient(firebaseMessaging)

    @Test
    fun `FCM 발송 성공 — send 호출 검증`() {
        every { firebaseMessaging.send(any()) } returns "projects/test/messages/12345"

        client.send(
            pushToken = "device-token",
            title = "새 인사가 왔어요!",
            body = "누군가 산책에 합류하고 싶어해요 🐕",
            data = mapOf("notificationId" to "1", "type" to "GREETING_RECEIVED"),
        )

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }

    @Test
    fun `FCM 발송 실패 시 예외 던지지 않음 — 로그만 기록`() {
        val exception = mockk<FirebaseMessagingException>(relaxed = true)
        every { firebaseMessaging.send(any()) } throws exception

        // 예외가 전파되지 않아야 한다
        client.send(
            pushToken = "invalid-token",
            title = "제목",
            body = "본문",
            data = emptyMap(),
        )

        verify(exactly = 1) { firebaseMessaging.send(any()) }
    }
}
