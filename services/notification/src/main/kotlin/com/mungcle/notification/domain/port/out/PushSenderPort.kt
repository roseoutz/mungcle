package com.mungcle.notification.domain.port.out

import com.mungcle.notification.domain.model.Notification

/**
 * 푸시 알림 발송 아웃바운드 포트.
 */
interface PushSenderPort {
    suspend fun sendPush(notification: Notification)
}
