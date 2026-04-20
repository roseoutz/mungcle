package com.mungcle.notification.infrastructure.grpc.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mungcle.notification.domain.port.`in`.ListNotificationsUseCase
import com.mungcle.notification.domain.port.`in`.MarkAllReadUseCase
import com.mungcle.notification.domain.port.`in`.MarkReadUseCase
import com.mungcle.proto.notification.v1.ListNotificationsRequest
import com.mungcle.proto.notification.v1.ListNotificationsResponse
import com.mungcle.proto.notification.v1.MarkAllReadRequest
import com.mungcle.proto.notification.v1.MarkAllReadResponse
import com.mungcle.proto.notification.v1.MarkReadRequest
import com.mungcle.proto.notification.v1.MarkReadResponse
import com.mungcle.proto.notification.v1.NotificationServiceGrpcKt
import com.mungcle.proto.notification.v1.NotificationType as ProtoNotificationType
import com.mungcle.proto.notification.v1.listNotificationsResponse
import com.mungcle.proto.notification.v1.markAllReadResponse
import com.mungcle.proto.notification.v1.markReadResponse
import com.mungcle.proto.notification.v1.notificationInfo
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class NotificationGrpcService(
    private val listNotificationsUseCase: ListNotificationsUseCase,
    private val markReadUseCase: MarkReadUseCase,
    private val markAllReadUseCase: MarkAllReadUseCase,
) : NotificationServiceGrpcKt.NotificationServiceCoroutineImplBase() {

    private val objectMapper = jacksonObjectMapper()

    override suspend fun listNotifications(request: ListNotificationsRequest): ListNotificationsResponse {
        val cursor = if (request.hasCursor()) request.cursor else null
        val limit = if (request.limit > 0) request.limit else 20
        val results = listNotificationsUseCase.execute(request.userId, cursor, limit)

        return listNotificationsResponse {
            this.notifications += results.map { r ->
                notificationInfo {
                    id = r.id
                    userId = r.userId
                    type = toProtoType(r.type)
                    payloadJson = objectMapper.writeValueAsString(r.payload)
                    read = r.isRead
                    createdAt = r.createdAt.epochSecond
                }
            }
            if (results.size == limit) {
                this.nextCursor = results.last().id
            }
        }
    }

    override suspend fun markRead(request: MarkReadRequest): MarkReadResponse {
        markReadUseCase.execute(request.notificationId, request.userId)
        return markReadResponse { }
    }

    override suspend fun markAllRead(request: MarkAllReadRequest): MarkAllReadResponse {
        markAllReadUseCase.execute(request.userId)
        return markAllReadResponse { }
    }

    private fun toProtoType(type: com.mungcle.notification.domain.model.NotificationType): ProtoNotificationType =
        when (type) {
            com.mungcle.notification.domain.model.NotificationType.GREETING_RECEIVED ->
                ProtoNotificationType.NOTIFICATION_TYPE_GREETING_RECEIVED
            com.mungcle.notification.domain.model.NotificationType.GREETING_ACCEPTED ->
                ProtoNotificationType.NOTIFICATION_TYPE_GREETING_ACCEPTED
            com.mungcle.notification.domain.model.NotificationType.MESSAGE_RECEIVED ->
                ProtoNotificationType.NOTIFICATION_TYPE_MESSAGE_RECEIVED
            com.mungcle.notification.domain.model.NotificationType.WALK_EXPIRED ->
                ProtoNotificationType.NOTIFICATION_TYPE_WALK_EXPIRED
        }
}
