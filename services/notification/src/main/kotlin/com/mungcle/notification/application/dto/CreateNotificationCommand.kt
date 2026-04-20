package com.mungcle.notification.application.dto

import com.mungcle.notification.domain.model.NotificationType

data class CreateNotificationCommand(
    val userId: Long,
    val type: NotificationType,
    val payload: Map<String, String> = emptyMap(),
    val eventId: String,
)
