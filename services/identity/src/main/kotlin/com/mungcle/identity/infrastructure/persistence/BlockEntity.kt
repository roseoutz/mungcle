package com.mungcle.identity.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "blocks", schema = "identity")
open class BlockEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "blocker_id", nullable = false)
    open var blockerId: Long = 0,

    @Column(name = "blocked_id", nullable = false)
    open var blockedId: Long = 0,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
