package com.mungcle.notification.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotificationSpringDataRepository : JpaRepository<NotificationEntity, Long> {

    /**
     * cursor 기반 페이지네이션 — cursor가 null이면 첫 페이지 조회.
     * createdAt DESC, id DESC 정렬로 일관된 커서 페이지네이션 보장.
     */
    @Query(
        """
        SELECT n FROM NotificationEntity n
        WHERE n.userId = :userId
          AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
        """
    )
    fun findByUserIdWithCursor(
        @Param("userId") userId: Long,
        @Param("cursor") cursor: Long?,
        pageable: org.springframework.data.domain.Pageable,
    ): List<NotificationEntity>

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = TRUE WHERE n.userId = :userId AND n.read = FALSE")
    fun markAllReadByUserId(@Param("userId") userId: Long)

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = TRUE WHERE n.id = :id")
    fun markReadById(@Param("id") id: Long)
}
