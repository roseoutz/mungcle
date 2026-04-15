package com.mungcle.identity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlockSpringDataRepository : JpaRepository<BlockEntity, Long> {
    fun findByBlockerId(blockerId: Long): List<BlockEntity>

    @Query(
        "SELECT DISTINCT CASE WHEN b.blockerId = :userId THEN b.blockedId ELSE b.blockerId END " +
            "FROM BlockEntity b WHERE b.blockerId = :userId OR b.blockedId = :userId"
    )
    fun findBlockedUserIds(userId: Long): List<Long>

    fun existsByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long): Boolean

    fun deleteByBlockerIdAndBlockedId(blockerId: Long, blockedId: Long)

    @Query(
        "SELECT COUNT(b) > 0 FROM BlockEntity b WHERE " +
            "(b.blockerId = :userIdA AND b.blockedId = :userIdB) OR " +
            "(b.blockerId = :userIdB AND b.blockedId = :userIdA)"
    )
    fun isBlocked(userIdA: Long, userIdB: Long): Boolean
}
