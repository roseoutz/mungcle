package com.mungcle.walks.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "walks", schema = "walks")
open class WalkEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "dog_id", nullable = false)
    open var dogId: Long = 0,

    @Column(name = "user_id", nullable = false)
    open var userId: Long = 0,

    @Column(name = "type", nullable = false, length = 10)
    open var type: String = "",

    @Column(name = "grid_cell", nullable = false, length = 30)
    open var gridCell: String = "",

    @Column(name = "status", nullable = false, length = 10)
    open var status: String = "ACTIVE",

    @Column(name = "started_at", nullable = false)
    open var startedAt: Instant = Instant.now(),

    @Column(name = "ends_at", nullable = false)
    open var endsAt: Instant = Instant.now(),

    @Column(name = "ended_at")
    open var endedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
