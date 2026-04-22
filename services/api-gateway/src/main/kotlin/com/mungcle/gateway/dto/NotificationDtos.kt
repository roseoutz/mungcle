package com.mungcle.gateway.dto

data class NotificationResponse(
    val id: Long,
    val userId: Long,
    val type: String,
    val payload: Map<String, Any>,
    val read: Boolean,
    val createdAt: Long,
)

data class NotificationsResponse(
    val notifications: List<NotificationResponse>,
    val nextCursor: Long?,
)
