package com.mungcle.notification.infrastructure.persistence

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class NotificationRepositoryAdapter(
    private val springDataRepository: NotificationSpringDataRepository,
) : NotificationRepositoryPort {

    override fun save(notification: Notification): Notification {
        val entity = NotificationMapper.toEntity(notification)
        val saved = springDataRepository.save(entity)
        return NotificationMapper.toDomain(saved)
    }

    override fun findById(id: Long): Notification? =
        springDataRepository.findById(id).orElse(null)?.let(NotificationMapper::toDomain)

    override fun findByUserId(userId: Long, cursor: Long?, limit: Int): List<Notification> =
        springDataRepository.findByUserIdWithCursor(userId, cursor, PageRequest.of(0, limit))
            .map(NotificationMapper::toDomain)

    override fun markAllReadByUserId(userId: Long) {
        springDataRepository.markAllReadByUserId(userId)
    }

    override fun markRead(id: Long) {
        springDataRepository.markReadById(id)
    }
}
