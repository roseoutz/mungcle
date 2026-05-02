package com.mungcle.notification.infrastructure.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.mungcle.notification.domain.port.out.PushNotificationPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Firebase Admin SDK를 사용한 FCM 푸시 알림 클라이언트.
 * 발송 실패 시 예외를 던지지 않고 로그만 기록한다 — 인앱 알림은 이미 저장됨.
 */
@Component
class FcmPushClient(
    private val firebaseMessaging: FirebaseMessaging,
) : PushNotificationPort {

    private val log = LoggerFactory.getLogger(FcmPushClient::class.java)

    override fun send(pushToken: String, title: String, body: String, data: Map<String, String>) {
        val message = Message.builder()
            .setToken(pushToken)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putAllData(data)
            .build()

        try {
            val messageId = firebaseMessaging.send(message)
            log.info("FCM 발송 성공: messageId={}", messageId)
        } catch (e: Exception) {
            log.warn("FCM 발송 실패 (인앱 알림 fallback): token={}, error={}", pushToken, e.message)
        }
    }
}
