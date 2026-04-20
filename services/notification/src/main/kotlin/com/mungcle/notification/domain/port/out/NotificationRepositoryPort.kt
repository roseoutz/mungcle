package com.mungcle.notification.domain.port.out

import com.mungcle.notification.domain.model.Notification

/**
 * 알림 저장소 아웃바운드 포트.
 */
interface NotificationRepositoryPort {
    suspend fun save(notification: Notification): Notification
    suspend fun findById(id: Long): Notification?
    suspend fun findByUserIdWithCursor(userId: Long, cursor: Long?, limit: Int): List<Notification>
    suspend fun markAllReadByUserId(userId: Long)
    suspend fun existsByEventId(eventId: String): Boolean
    suspend fun saveEventId(eventId: String)
}
