package com.mungcle.gateway.infrastructure.grpc

import com.mungcle.proto.notification.v1.ListNotificationsResponse
import com.mungcle.proto.notification.v1.NotificationServiceGrpcKt
import com.mungcle.proto.notification.v1.listNotificationsRequest
import com.mungcle.proto.notification.v1.markAllReadRequest
import com.mungcle.proto.notification.v1.markReadRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class NotificationClient(
    @GrpcClient("notification") private val stub: NotificationServiceGrpcKt.NotificationServiceCoroutineStub,
) {

    suspend fun listNotifications(userId: Long, cursor: Long?, limit: Int): ListNotificationsResponse =
        stub.listNotifications(listNotificationsRequest {
            this.userId = userId
            if (cursor != null) this.cursor = cursor
            this.limit = limit
        })

    suspend fun markRead(notificationId: Long, userId: Long) {
        stub.markRead(markReadRequest {
            this.notificationId = notificationId
            this.userId = userId
        })
    }

    suspend fun markAllRead(userId: Long) {
        stub.markAllRead(markAllReadRequest {
            this.userId = userId
        })
    }
}
