package com.mungcle.walks.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "walk_patterns", schema = "walks")
open class WalkPatternEntity(
    @Id
    @Tsid
    open var id: Long = 0,

    @Column(name = "grid_cell", nullable = false)
    open var gridCell: String = "",

    @Column(name = "hour_of_day", nullable = false)
    open var hourOfDay: Int = 0,

    @Column(name = "dog_id", nullable = false)
    open var dogId: Long = 0,

    @Column(name = "walk_count", nullable = false)
    open var walkCount: Int = 1,

    @Column(name = "last_walked_at", nullable = false)
    open var lastWalkedAt: Instant = Instant.now(),
)
