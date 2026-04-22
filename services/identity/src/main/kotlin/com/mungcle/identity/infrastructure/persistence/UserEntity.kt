package com.mungcle.identity.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users", schema = "identity")
open class UserEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "social_provider", length = 10)
    open var socialProvider: String? = null,

    @Column(name = "social_id")
    open var socialId: String? = null,

    @Column(name = "email", unique = true)
    open var email: String? = null,

    @Column(name = "password_hash")
    open var passwordHash: String? = null,

    @Column(name = "nickname", nullable = false, length = 16)
    open var nickname: String = "",

    @Column(name = "neighborhood", length = 100)
    open var neighborhood: String? = null,

    @Column(name = "push_token")
    open var pushToken: String? = null,

    @Column(name = "profile_photo_path", length = 500)
    open var profilePhotoPath: String? = null,

    @Column(name = "flagged_for_review", nullable = false)
    open var flaggedForReview: Boolean = false,

    @Column(name = "deleted_at")
    open var deletedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)
