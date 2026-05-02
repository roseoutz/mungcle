package com.mungcle.notification.infrastructure.event

import com.mungcle.notification.domain.event.NotificationCreatedEvent
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationPushTemplate
import com.mungcle.notification.domain.port.out.PushNotificationPort
import com.mungcle.notification.domain.port.out.UserPushTokenPort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 알림 저장 트랜잭션 커밋 이후 FCM 푸시를 발송한다.
 * 트랜잭션 안에서 외부 호출(gRPC/FCM)을 수행하면 DB 커넥션 풀이 고갈될 수 있으므로
 * AFTER_COMMIT 시점으로 분리한다.
 */
@Component
class NotificationPushDispatcher(
    private val userPushTokenPort: UserPushTokenPort,
    private val pushNotificationPort: PushNotificationPort,
) {

    private val log = LoggerFactory.getLogger(NotificationPushDispatcher::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onNotificationCreated(event: NotificationCreatedEvent) {
        try {
            sendPush(event.notification)
        } catch (e: Exception) {
            log.error("FCM 푸시 발송 실패 — 인앱 알림은 정상 저장됨: userId={}", event.notification.userId, e)
        }
    }

    private fun sendPush(notification: Notification) {
        val pushToken = runBlocking {
            userPushTokenPort.getPushToken(notification.userId)
        }
        if (pushToken == null) {
            log.debug("pushToken 없음 — 인앱 알림만 저장: userId={}", notification.userId)
            return
        }
        val content = NotificationPushTemplate.of(notification.type)
        pushNotificationPort.send(
            pushToken = pushToken,
            title = content.title,
            body = content.body,
            data = mapOf(
                "notificationId" to notification.id.toString(),
                "type" to notification.type.name,
            ),
        )
    }
}
