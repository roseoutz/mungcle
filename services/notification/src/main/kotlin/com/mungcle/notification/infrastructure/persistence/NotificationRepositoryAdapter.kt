package com.mungcle.notification.infrastructure.persistence

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class NotificationRepositoryAdapter(
    private val notificationSpringData: NotificationSpringDataRepository,
    private val processedEventSpringData: ProcessedEventSpringDataRepository,
) : NotificationRepositoryPort {

    override suspend fun save(notification: Notification): Notification {
        val entity = NotificationMapper.toEntity(notification)
        val saved = notificationSpringData.save(entity)
        return NotificationMapper.toDomain(saved)
    }

    override suspend fun findById(id: Long): Notification? =
        notificationSpringData.findById(id).orElse(null)?.let(NotificationMapper::toDomain)

    override suspend fun findByUserIdWithCursor(userId: Long, cursor: Long?, limit: Int): List<Notification> {
        val pageable = PageRequest.of(0, limit)
        return notificationSpringData.findByUserIdWithCursor(userId, cursor, pageable)
            .map(NotificationMapper::toDomain)
    }

    override suspend fun markAllReadByUserId(userId: Long) {
        notificationSpringData.markAllReadByUserId(userId)
    }

    override suspend fun existsByEventId(eventId: String): Boolean =
        processedEventSpringData.existsById(eventId)

    override suspend fun saveEventId(eventId: String) {
        processedEventSpringData.save(ProcessedEventEntity(eventId = eventId))
    }
}
