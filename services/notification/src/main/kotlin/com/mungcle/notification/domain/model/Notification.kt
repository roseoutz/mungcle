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
    val payloadJson: String,
    val read: Boolean = false,
    val createdAt: Instant = Instant.now(),
)
