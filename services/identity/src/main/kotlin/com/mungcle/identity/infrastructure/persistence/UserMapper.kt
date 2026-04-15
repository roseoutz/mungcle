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
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        kakaoId = domain.kakaoId,
        email = domain.email,
        passwordHash = domain.passwordHash,
        nickname = domain.nickname,
        pushToken = domain.pushToken,
        createdAt = domain.createdAt,
    )
}
