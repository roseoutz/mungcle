package com.mungcle.notification.infrastructure.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationSpringDataRepository : JpaRepository<NotificationEntity, Long> {

    @Query(
        "SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND (:cursor IS NULL OR n.id < :cursor) ORDER BY n.createdAt DESC"
    )
    fun findByUserIdWithCursor(userId: Long, cursor: Long?, pageable: Pageable): List<NotificationEntity>

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.readAt IS NULL")
    fun markAllReadByUserId(userId: Long)
}
