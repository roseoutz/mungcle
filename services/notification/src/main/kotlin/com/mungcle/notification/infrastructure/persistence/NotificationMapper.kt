package com.mungcle.notification.infrastructure.persistence

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType

object NotificationMapper {

    fun toDomain(entity: NotificationEntity): Notification = Notification(
        id = entity.id,
        userId = entity.userId,
        type = NotificationType.valueOf(entity.type),
        payload = entity.payload,
        readAt = entity.readAt,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Notification): NotificationEntity = NotificationEntity(
        id = domain.id,
        userId = domain.userId,
        type = domain.type.name,
        payload = domain.payload,
        readAt = domain.readAt,
        createdAt = domain.createdAt,
    )
}
