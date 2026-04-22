package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.Message

object MessageMapper {

    fun toDomain(entity: MessageEntity): Message = Message(
        id = entity.id,
        greetingId = entity.greetingId,
        senderUserId = entity.senderUserId,
        body = entity.body,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Message): MessageEntity = MessageEntity(
        id = domain.id,
        greetingId = domain.greetingId,
        senderUserId = domain.senderUserId,
        body = domain.body,
        createdAt = domain.createdAt,
    )
}
