package com.mungcle.notification.infrastructure.grpc.server

import com.mungcle.notification.domain.exception.NotificationException
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.ListNotificationsUseCase
import com.mungcle.notification.domain.port.`in`.MarkAllReadUseCase
import com.mungcle.notification.domain.port.`in`.MarkReadUseCase
import com.mungcle.proto.notification.v1.ListNotificationsRequest
import com.mungcle.proto.notification.v1.ListNotificationsResponse
import com.mungcle.proto.notification.v1.MarkAllReadRequest
import com.mungcle.proto.notification.v1.MarkAllReadResponse
import com.mungcle.proto.notification.v1.MarkReadRequest
import com.mungcle.proto.notification.v1.MarkReadResponse
import com.mungcle.proto.notification.v1.NotificationInfo
import com.mungcle.proto.notification.v1.NotificationServiceGrpcKt
import com.mungcle.proto.notification.v1.listNotificationsResponse
import com.mungcle.proto.notification.v1.notificationInfo
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class NotificationGrpcService(
    private val listNotificationsUseCase: ListNotificationsUseCase,
    private val markReadUseCase: MarkReadUseCase,
    private val markAllReadUseCase: MarkAllReadUseCase,
) : NotificationServiceGrpcKt.NotificationServiceCoroutineImplBase() {

    override suspend fun listNotifications(request: ListNotificationsRequest): ListNotificationsResponse {
        try {
            val result = listNotificationsUseCase.execute(
                ListNotificationsUseCase.Query(
                    userId = request.userId,
                    cursor = if (request.hasCursor()) request.cursor else null,
                    limit = request.limit.takeIf { it > 0 } ?: 20,
                )
            )
            return listNotificationsResponse {
                notifications += result.notifications.map { it.toProto() }
                result.nextCursor?.let { nextCursor = it }
            }
        } catch (e: NotificationException) {
            throw e.toStatusException()
        }
    }

    override suspend fun markRead(request: MarkReadRequest): MarkReadResponse {
        try {
            markReadUseCase.execute(
                MarkReadUseCase.Command(
                    notificationId = request.notificationId,
                    userId = request.userId,
                )
            )
            return MarkReadResponse.getDefaultInstance()
        } catch (e: NotificationException) {
            throw e.toStatusException()
        }
    }

    override suspend fun markAllRead(request: MarkAllReadRequest): MarkAllReadResponse {
        try {
            markAllReadUseCase.execute(MarkAllReadUseCase.Command(userId = request.userId))
            return MarkAllReadResponse.getDefaultInstance()
        } catch (e: NotificationException) {
            throw e.toStatusException()
        }
    }

    private fun Notification.toProto(): NotificationInfo = notificationInfo {
        id = this@toProto.id
        userId = this@toProto.userId
        type = this@toProto.type.toProto()
        payloadJson = this@toProto.payloadJson
        read = this@toProto.read
        createdAt = this@toProto.createdAt.epochSecond
    }

    private fun NotificationType.toProto(): com.mungcle.proto.notification.v1.NotificationType = when (this) {
        NotificationType.GREETING_RECEIVED -> com.mungcle.proto.notification.v1.NotificationType.NOTIFICATION_TYPE_GREETING_RECEIVED
        NotificationType.GREETING_ACCEPTED -> com.mungcle.proto.notification.v1.NotificationType.NOTIFICATION_TYPE_GREETING_ACCEPTED
        NotificationType.MESSAGE_RECEIVED -> com.mungcle.proto.notification.v1.NotificationType.NOTIFICATION_TYPE_MESSAGE_RECEIVED
        NotificationType.WALK_EXPIRED -> com.mungcle.proto.notification.v1.NotificationType.NOTIFICATION_TYPE_WALK_EXPIRED
    }
}
