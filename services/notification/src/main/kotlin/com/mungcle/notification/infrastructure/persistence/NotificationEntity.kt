package com.mungcle.notification.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
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
    @Enumerated(EnumType.STRING)
    open var type: com.mungcle.notification.domain.model.NotificationType =
        com.mungcle.notification.domain.model.NotificationType.GREETING_RECEIVED,

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    open var payloadJson: String = "{}",

    @Column(name = "read", nullable = false)
    open var read: Boolean = false,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
