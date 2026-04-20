package com.mungcle.notification.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "processed_events", schema = "notification")
open class ProcessedEventEntity(
    @Id
    @Column(name = "event_id", nullable = false, length = 100)
    open var eventId: String = "",

    @Column(name = "processed_at", nullable = false)
    open var processedAt: Instant = Instant.now(),
)
