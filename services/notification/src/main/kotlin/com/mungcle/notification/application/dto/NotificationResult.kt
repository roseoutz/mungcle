package com.mungcle.notification.application.dto

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import java.time.Instant

data class NotificationResult(
    val id: Long,
    val userId: Long,
    val type: NotificationType,
    val payload: Map<String, String>,
    val isRead: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(notification: Notification): NotificationResult = NotificationResult(
            id = notification.id,
            userId = notification.userId,
            type = notification.type,
            payload = notification.payload,
            isRead = notification.isRead(),
            createdAt = notification.createdAt,
        )
    }
}
