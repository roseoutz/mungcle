package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.NotificationClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.notification.v1.NotificationType
import com.mungcle.proto.notification.v1.listNotificationsResponse
import com.mungcle.proto.notification.v1.notificationInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NotificationController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var notificationClient: NotificationClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    private val fakeNotification = notificationInfo {
        id = 300L
        userId = 10L
        type = NotificationType.NOTIFICATION_TYPE_GREETING_RECEIVED
        payloadJson = """{"greetingId": 200}"""
        read = false
        createdAt = 1700000000L
    }

    private fun setupAuth(userId: Long = 10L) {
        coEvery { identityClient.validateToken(any()) } returns validateTokenResponse {
            this.userId = userId
            valid = true
        }
    }

    @Test
    fun `알림 목록 조회 — 200 반환`() {
        setupAuth()
        coEvery { notificationClient.listNotifications(10L, null, 20) } returns listNotificationsResponse {
            notifications += fakeNotification
        }

        mockMvc.perform(
            get("/api/notifications")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notifications[0].id").value(300L))
            .andExpect(jsonPath("$.notifications[0].type").value("GREETING_RECEIVED"))
            .andExpect(jsonPath("$.notifications[0].read").value(false))
    }

    @Test
    fun `알림 읽음 처리 — 204 반환`() {
        setupAuth()
        coEvery { notificationClient.markRead(300L, 10L) } returns Unit

        mockMvc.perform(
            post("/api/notifications/300/read")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isUnauthorized)
    }
}
