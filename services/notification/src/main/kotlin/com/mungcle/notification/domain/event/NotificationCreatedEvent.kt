package com.mungcle.notification.domain.event

import com.mungcle.notification.domain.model.Notification

data class NotificationCreatedEvent(
    val notification: Notification,
)
