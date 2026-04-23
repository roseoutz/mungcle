package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.NotificationClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.resilience.FallbackResult
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.notification.v1.NotificationType
import com.mungcle.proto.notification.v1.listNotificationsResponse
import com.mungcle.proto.notification.v1.notificationInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(NotificationController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class NotificationControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var notificationClient: NotificationClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @MockkBean
    private lateinit var cb: CircuitBreakerWrapper

    @BeforeEach
    fun setupCb() {
        coEvery { cb.execute(any(), any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>()() }
        coEvery { cb.executeWithFallback(any(), any(), any<suspend () -> Any?>()) } coAnswers {
            FallbackResult(thirdArg<suspend () -> Any?>()(), isFallback = false)
        }
    }

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

        webTestClient.get().uri("/v1/notifications")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.notifications[0].id").isEqualTo(300L)
            .jsonPath("$.notifications[0].type").isEqualTo("GREETING_RECEIVED")
            .jsonPath("$.notifications[0].read").isEqualTo(false)
    }

    @Test
    fun `알림 읽음 처리 — 204 반환`() {
        setupAuth()
        coEvery { notificationClient.markRead(300L, 10L) } returns Unit

        webTestClient.post().uri("/v1/notifications/300/read")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        webTestClient.get().uri("/v1/notifications")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
