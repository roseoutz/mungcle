package com.mungcle.notification.domain.model

import java.time.Instant

/**
 * 알림 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Notification(
    val id: Long = 0,
    val userId: Long,
    val type: NotificationType,
    val payload: Map<String, String> = emptyMap(),
    val readAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    fun isRead(): Boolean = readAt != null

    fun markRead(now: Instant): Notification = copy(readAt = now)
}
