package com.mungcle.notification.domain.model

import java.time.Instant

/**
 * 알림 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 *
 * read 상태는 [markAsRead]를 통해서만 변경할 수 있다.
 */
class Notification(
    val id: Long = 0,
    val userId: Long,
    val type: NotificationType,
    val payloadJson: String,
    read: Boolean = false,
    val createdAt: Instant = Instant.now(),
) {
    var read: Boolean = read
        private set

    /**
     * 알림을 읽음 상태로 표시한다.
     * 이미 읽은 알림에 대해 호출해도 안전하다.
     */
    fun markAsRead() {
        read = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Notification) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "Notification(id=$id, userId=$userId, type=$type, read=$read, createdAt=$createdAt)"
}
