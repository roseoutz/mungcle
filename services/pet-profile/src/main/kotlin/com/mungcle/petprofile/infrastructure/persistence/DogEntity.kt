package com.mungcle.petprofile.infrastructure.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "dogs", schema = "pet_profile")
open class DogEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    open var id: Long = 0,

    @Column(name = "owner_id", nullable = false)
    open var ownerId: Long = 0,

    @Column(name = "name", nullable = false, length = 50)
    open var name: String = "",

    @Column(name = "breed", nullable = false, length = 100)
    open var breed: String = "",

    @Column(name = "size", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    open var size: DogSizeEntity = DogSizeEntity.MEDIUM,

    @Column(name = "temperaments", nullable = false, columnDefinition = "TEXT[]")
    open var temperaments: Array<String> = emptyArray(),

    @Column(name = "sociability", nullable = false)
    open var sociability: Int = 3,

    @Column(name = "photo_path", length = 500)
    open var photoPath: String? = null,

    @Column(name = "vaccination_photo_path", length = 500)
    open var vaccinationPhotoPath: String? = null,

    @Column(name = "deleted_at")
    open var deletedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant = Instant.now(),
)

enum class DogSizeEntity {
    SMALL,
    MEDIUM,
    LARGE,
}
