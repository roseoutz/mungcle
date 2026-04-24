package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateReportRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coJustRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(ReportController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class ReportControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @MockkBean
    private lateinit var cb: CircuitBreakerWrapper

    @BeforeEach
    fun setupCb() {
        coEvery { cb.execute(any(), any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>()() }
    }

    private fun setupAuth(userId: Long = 1L) {
        val tokenResponse = validateTokenResponse {
            this.userId = userId
            valid = true
        }
        coEvery { identityClient.validateToken(any()) } returns tokenResponse
    }

    @Test
    fun `신고 생성 성공 — 201 반환`() {
        setupAuth()
        coJustRun { identityClient.createReport(any(), any(), any()) }
        val req = CreateReportRequest(reportedUserId = 99L, reason = "욕설/혐오 발언")

        webTestClient.post().uri("/v1/reports")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `reason 미입력 시 400 반환`() {
        setupAuth()
        val body = """{"reportedUserId": 99, "reason": ""}"""

        webTestClient.post().uri("/v1/reports")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `reportedUserId 누락 시 400 반환`() {
        setupAuth()
        val body = """{"reason": "스팸/광고"}"""

        webTestClient.post().uri("/v1/reports")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `비인증 신고 요청 — 401 반환`() {
        webTestClient.post().uri("/v1/reports")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"reportedUserId": 99, "reason": "스팸/광고"}""")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
