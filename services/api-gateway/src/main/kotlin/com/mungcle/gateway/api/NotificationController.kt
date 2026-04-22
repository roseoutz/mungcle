package com.mungcle.gateway.api

import com.mungcle.gateway.dto.NotificationResponse
import com.mungcle.gateway.dto.NotificationsResponse
import com.mungcle.gateway.infrastructure.grpc.NotificationClient
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationClient: NotificationClient,
) {

    private val objectMapper = jacksonObjectMapper()

    @GetMapping
    fun listNotifications(
        @AuthUser userId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") limit: Int,
    ): NotificationsResponse = runBlocking {
        val response = notificationClient.listNotifications(userId, cursor, limit)
        NotificationsResponse(
            notifications = response.notificationsList.map { notification ->
                @Suppress("UNCHECKED_CAST")
                val payload = objectMapper.readValue(notification.payloadJson, Map::class.java) as Map<String, Any>
                NotificationResponse(
                    id = notification.id,
                    userId = notification.userId,
                    type = notification.type.name.removePrefix("NOTIFICATION_TYPE_"),
                    payload = payload,
                    read = notification.read,
                    createdAt = notification.createdAt,
                )
            },
            nextCursor = if (response.hasNextCursor()) response.nextCursor else null,
        )
    }

    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markRead(@AuthUser userId: Long, @PathVariable id: Long): Unit = runBlocking {
        notificationClient.markRead(notificationId = id, userId = userId)
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun markAllRead(@AuthUser userId: Long): Unit = runBlocking {
        notificationClient.markAllRead(userId)
    }
}
