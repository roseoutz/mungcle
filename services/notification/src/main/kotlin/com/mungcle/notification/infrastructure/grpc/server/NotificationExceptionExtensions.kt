package com.mungcle.notification.infrastructure.grpc.server

import com.mungcle.notification.domain.exception.NotificationException
import com.mungcle.notification.domain.exception.NotificationNotFoundException
import com.mungcle.notification.domain.exception.NotificationNotOwnedException
import io.grpc.Status
import io.grpc.StatusException

fun NotificationException.toStatusException(): StatusException = when (this) {
    is NotificationNotFoundException -> StatusException(Status.NOT_FOUND.withDescription(message))
    is NotificationNotOwnedException -> StatusException(Status.PERMISSION_DENIED.withDescription(message))
}
