package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.Greeting

object GreetingMapper {

    fun toDomain(entity: GreetingEntity): Greeting = Greeting(
        id = entity.id,
        senderUserId = entity.senderUserId,
        senderDogId = entity.senderDogId,
        receiverUserId = entity.receiverUserId,
        receiverDogId = entity.receiverDogId,
        receiverWalkId = entity.receiverWalkId,
        status = entity.status,
        createdAt = entity.createdAt,
        respondedAt = entity.respondedAt,
        expiresAt = entity.expiresAt,
    )

    fun toEntity(domain: Greeting): GreetingEntity = GreetingEntity(
        id = domain.id,
        senderUserId = domain.senderUserId,
        senderDogId = domain.senderDogId,
        receiverUserId = domain.receiverUserId,
        receiverDogId = domain.receiverDogId,
        receiverWalkId = domain.receiverWalkId,
        status = domain.status,
        createdAt = domain.createdAt,
        respondedAt = domain.respondedAt,
        expiresAt = domain.expiresAt,
    )
}
