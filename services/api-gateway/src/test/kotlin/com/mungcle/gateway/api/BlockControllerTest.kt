package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateBlockRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.blockInfo
import com.mungcle.proto.identity.v1.listBlocksResponse
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

@WebFluxTest(BlockController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class BlockControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @MockkBean
    private lateinit var cb: CircuitBreakerWrapper

    @BeforeEach
    fun setupCb() {
        coEvery { cb.execute(any(), any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>()() }
        // executeWithFallback 기본 동작 — block 실행 (CB CLOSED 시)
        coEvery { cb.executeWithFallback(any(), any(), any(), any<suspend () -> Any?>()) } coAnswers {
            arg<suspend () -> Any?>(3)()
        }
    }

    private fun setupAuth(userId: Long = 1L) {
        val tokenResponse = validateTokenResponse {
            this.userId = userId
            valid = true
        }
        coEvery { identityClient.validateToken(any()) } returns tokenResponse
    }

    @Test
    fun `차단 생성 성공 — 201 반환`() {
        setupAuth()
        coJustRun { identityClient.createBlock(any(), any()) }
        val req = CreateBlockRequest(blockedUserId = 99L)

        webTestClient.post().uri("/v1/blocks")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `차단 목록 조회 성공 — 200 반환`() {
        setupAuth()
        val response = listBlocksResponse {
            blocks += blockInfo {
                blockedUserId = 99L
                blockedNickname = "blocked_user"
                createdAt = 1000L
            }
        }
        coEvery { identityClient.listBlocks(1L) } returns response

        webTestClient.get().uri("/v1/blocks")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].blockedUserId").isEqualTo(99)
            .jsonPath("$[0].blockedNickname").isEqualTo("blocked_user")
    }

    @Test
    fun `차단 해제 성공 — 204 반환`() {
        setupAuth()
        coJustRun { identityClient.deleteBlock(any(), any()) }

        webTestClient.delete().uri("/v1/blocks/99")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `차단 목록 — CB OPEN 시 빈 배열과 X-Fallback 헤더 반환`() {
        setupAuth()
        coEvery {
            cb.executeWithFallback(any(), any(), any(), any<suspend () -> Any?>())
        } coAnswers {
            val onFallback = thirdArg<suspend () -> Unit>()
            onFallback()
            null as Any? // fallback 값
        }

        webTestClient.get().uri("/v1/blocks")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Fallback", "true")
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `비인증 차단 목록 조회 — 401 반환`() {
        webTestClient.get().uri("/v1/blocks")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
