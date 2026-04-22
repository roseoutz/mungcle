package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.GreetingStatus
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "greetings",
    schema = "social",
    uniqueConstraints = [UniqueConstraint(columnNames = ["sender_user_id", "receiver_walk_id"])],
)
open class GreetingEntity(
    @Id
    @Tsid
    open var id: Long = 0,

    @Column(name = "sender_user_id", nullable = false)
    open var senderUserId: Long = 0,

    @Column(name = "sender_dog_id", nullable = false)
    open var senderDogId: Long = 0,

    @Column(name = "receiver_user_id", nullable = false)
    open var receiverUserId: Long = 0,

    @Column(name = "receiver_dog_id", nullable = false)
    open var receiverDogId: Long = 0,

    @Column(name = "receiver_walk_id", nullable = false)
    open var receiverWalkId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var status: GreetingStatus = GreetingStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),

    @Column(name = "responded_at")
    open var respondedAt: Instant? = null,

    @Column(name = "expires_at", nullable = false)
    open var expiresAt: Instant = Instant.now(),
)
