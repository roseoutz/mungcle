package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.User

object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id,
        kakaoId = entity.kakaoId,
        email = entity.email,
        passwordHash = entity.passwordHash,
        nickname = entity.nickname,
        pushToken = entity.pushToken,
        neighborhood = entity.neighborhood,
        profilePhotoPath = entity.profilePhotoPath,
        flaggedForReview = entity.flaggedForReview,
        deletedAt = entity.deletedAt,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        kakaoId = domain.kakaoId,
        email = domain.email,
        passwordHash = domain.passwordHash,
        nickname = domain.nickname,
        pushToken = domain.pushToken,
        neighborhood = domain.neighborhood,
        profilePhotoPath = domain.profilePhotoPath,
        flaggedForReview = domain.flaggedForReview,
        deletedAt = domain.deletedAt,
        createdAt = domain.createdAt,
    )
}
