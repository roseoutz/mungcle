package com.mungcle.notification.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.time.Instant

@Entity
@Table(name = "notifications", schema = "notification")
open class NotificationEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "user_id", nullable = false)
    open var userId: Long = 0,

    @Column(name = "type", nullable = false, length = 30)
    open var type: String = "",

    @Type(JsonType::class)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    open var payload: Map<String, String> = emptyMap(),

    @Column(name = "read_at")
    open var readAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
