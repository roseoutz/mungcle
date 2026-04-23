package com.mungcle.gateway.api

import com.mungcle.gateway.dto.NotificationResponse
import com.mungcle.gateway.dto.NotificationsResponse
import com.mungcle.gateway.infrastructure.grpc.NotificationClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/v1/notifications")
class NotificationController(
    private val notificationClient: NotificationClient,
    private val cb: CircuitBreakerWrapper,
) {

    private val objectMapper = jacksonObjectMapper()

    @GetMapping
    suspend fun listNotifications(
        @AuthUser userId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") limit: Int,
        exchange: ServerWebExchange,
    ): NotificationsResponse {
        // CB OPEN 시 빈 목록 반환 — X-Fallback 헤더로 클라이언트에 알림
        val response = cb.executeWithFallback(
            name = "notification-service",
            fallback = null,
            onFallback = { exchange.response.headers.set("X-Fallback", "true") },
        ) { notificationClient.listNotifications(userId, cursor, limit) }
            ?: return NotificationsResponse(notifications = emptyList(), nextCursor = null)

        return NotificationsResponse(
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
    suspend fun markRead(@AuthUser userId: Long, @PathVariable id: Long) {
        cb.execute("notification-service") { notificationClient.markRead(notificationId = id, userId = userId) }
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun markAllRead(@AuthUser userId: Long) {
        cb.execute("notification-service") { notificationClient.markAllRead(userId) }
    }
}
