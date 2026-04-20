package com.mungcle.notification.domain.exception

sealed class NotificationException(message: String) : RuntimeException(message)

class NotificationNotFoundException(id: Long) :
    NotificationException("알림을 찾을 수 없습니다: $id")

class NotificationNotOwnedException(notificationId: Long, userId: Long) :
    NotificationException("알림 $notificationId 에 대한 권한이 없습니다 (userId=$userId)")
