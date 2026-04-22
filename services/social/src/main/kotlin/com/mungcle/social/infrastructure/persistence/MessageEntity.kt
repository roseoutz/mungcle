package com.mungcle.social.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "messages", schema = "social")
open class MessageEntity(
    @Id
    @Tsid
    open var id: Long = 0,

    @Column(name = "greeting_id", nullable = false)
    open var greetingId: Long = 0,

    @Column(name = "sender_user_id", nullable = false)
    open var senderUserId: Long = 0,

    @Column(name = "body", nullable = false, length = 140)
    open var body: String = "",

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
