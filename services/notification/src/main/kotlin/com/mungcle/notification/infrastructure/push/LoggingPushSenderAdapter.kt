package com.mungcle.notification.infrastructure.push

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.out.PushSenderPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * MVP 푸시 발송 어댑터 — 실제 FCM 대신 로그만 출력.
 */
@Component
class LoggingPushSenderAdapter : PushSenderPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun sendPush(notification: Notification, pushToken: String) {
        log.info(
            "[MVP Push] userId={}, type={}, payload={}, pushToken={}",
            notification.userId,
            notification.type,
            notification.payload,
            pushToken,
        )
    }
}
