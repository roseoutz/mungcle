package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.GreetingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional

interface GreetingSpringDataRepository : JpaRepository<GreetingEntity, Long> {

    @Query(
        "SELECT g FROM GreetingEntity g WHERE g.senderUserId = :senderUserId AND g.receiverWalkId = :receiverWalkId"
    )
    fun findBySenderAndWalk(
        @Param("senderUserId") senderUserId: Long,
        @Param("receiverWalkId") receiverWalkId: Long,
    ): Optional<GreetingEntity>

    @Query(
        """SELECT g FROM GreetingEntity g
           WHERE (g.senderUserId = :userId OR g.receiverUserId = :userId)
             AND (:statusFilter IS NULL OR g.status = :statusFilter)
             AND (:isSender IS NULL
                  OR (:isSender = TRUE AND g.senderUserId = :userId)
                  OR (:isSender = FALSE AND g.receiverUserId = :userId))"""
    )
    fun findByUserId(
        @Param("userId") userId: Long,
        @Param("statusFilter") statusFilter: GreetingStatus?,
        @Param("isSender") isSender: Boolean?,
    ): List<GreetingEntity>

    @Query("SELECT g FROM GreetingEntity g WHERE g.status = 'PENDING' AND g.expiresAt < :now")
    fun findExpiredPending(@Param("now") now: Instant): List<GreetingEntity>

    @Query("SELECT g FROM GreetingEntity g WHERE g.status = 'ACCEPTED' AND g.expiresAt < :now")
    fun findExpiredAccepted(@Param("now") now: Instant): List<GreetingEntity>
}
