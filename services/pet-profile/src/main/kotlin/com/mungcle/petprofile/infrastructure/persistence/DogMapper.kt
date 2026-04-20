package com.mungcle.petprofile.infrastructure.persistence

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament

object DogMapper {
    fun toDomain(entity: DogEntity): Dog = Dog(
        id = entity.id,
        ownerId = entity.ownerId,
        name = entity.name,
        breed = entity.breed,
        size = DogSize.valueOf(entity.size),
        temperaments = entity.temperaments.map { Temperament.valueOf(it) },
        sociability = entity.sociability,
        photoPath = entity.photoPath,
        vaccinationPhotoPath = entity.vaccinationPhotoPath,
        deletedAt = entity.deletedAt,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Dog): DogEntity = DogEntity(
        id = domain.id,
        ownerId = domain.ownerId,
        name = domain.name,
        breed = domain.breed,
        size = domain.size.name,
        temperaments = domain.temperaments.map { it.name }.toTypedArray(),
        sociability = domain.sociability,
        photoPath = domain.photoPath,
        vaccinationPhotoPath = domain.vaccinationPhotoPath,
        deletedAt = domain.deletedAt,
        createdAt = domain.createdAt,
    )
}
