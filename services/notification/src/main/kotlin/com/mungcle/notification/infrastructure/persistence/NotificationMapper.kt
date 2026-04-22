package com.mungcle.notification.infrastructure.persistence

import com.mungcle.notification.domain.model.Notification

object NotificationMapper {

    fun toDomain(entity: NotificationEntity): Notification = Notification(
        id = entity.id,
        userId = entity.userId,
        type = entity.type,
        payloadJson = entity.payloadJson,
        read = entity.read,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Notification): NotificationEntity = NotificationEntity(
        id = domain.id,
        userId = domain.userId,
        type = domain.type,
        payloadJson = domain.payloadJson,
        read = domain.read,
        createdAt = domain.createdAt,
    )
}
