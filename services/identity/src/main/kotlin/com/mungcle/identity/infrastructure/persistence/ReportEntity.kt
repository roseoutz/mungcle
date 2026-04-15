package com.mungcle.identity.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "reports", schema = "identity")
open class ReportEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "reporter_id", nullable = false)
    open var reporterId: Long = 0,

    @Column(name = "reported_id", nullable = false)
    open var reportedId: Long = 0,

    @Column(name = "reason", nullable = false, length = 500)
    open var reason: String = "",

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
