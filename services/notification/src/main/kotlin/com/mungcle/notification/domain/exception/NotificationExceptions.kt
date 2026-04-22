package com.mungcle.notification.domain.exception

sealed class NotificationException(message: String) : RuntimeException(message)

class NotificationNotFoundException(notificationId: Long) :
    NotificationException("알림을 찾을 수 없습니다: $notificationId")

class NotificationNotOwnedException(notificationId: Long, userId: Long) :
    NotificationException("해당 알림의 소유자가 아닙니다: notificationId=$notificationId, userId=$userId")
