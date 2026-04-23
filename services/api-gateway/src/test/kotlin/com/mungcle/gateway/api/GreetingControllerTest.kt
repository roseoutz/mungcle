package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateGreetingRequest
import com.mungcle.gateway.dto.RespondGreetingRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.SocialClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.social.v1.GreetingStatus
import com.mungcle.proto.social.v1.greetingInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(GreetingController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class GreetingControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var socialClient: SocialClient

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

    private val fakeGreeting = greetingInfo {
        id = 200L
        senderUserId = 10L
        receiverUserId = 20L
        senderDogId = 1L
        receiverDogId = 2L
        receiverWalkId = 100L
        status = GreetingStatus.GREETING_STATUS_PENDING
        createdAt = 1700000000L
    }

    private fun setupAuth(userId: Long = 10L) {
        coEvery { identityClient.validateToken(any()) } returns validateTokenResponse {
            this.userId = userId
            valid = true
        }
    }

    @Test
    fun `인사 생성 성공 — 201 반환`() {
        setupAuth()
        val req = CreateGreetingRequest(senderDogId = 1L, receiverWalkId = 100L)
        coEvery { socialClient.createGreeting(10L, 1L, 100L) } returns fakeGreeting

        webTestClient.post().uri("/v1/greetings")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(200L)
            .jsonPath("$.status").isEqualTo("PENDING")
    }

    @Test
    fun `인사 수락 — 200 반환`() {
        setupAuth()
        val acceptedGreeting = greetingInfo {
            id = 200L
            senderUserId = 10L
            receiverUserId = 20L
            senderDogId = 1L
            receiverDogId = 2L
            receiverWalkId = 100L
            status = GreetingStatus.GREETING_STATUS_ACCEPTED
            createdAt = 1700000000L
            respondedAt = 1700000100L
        }
        val req = RespondGreetingRequest(accept = true)
        coEvery { socialClient.respondGreeting(200L, 10L, true) } returns acceptedGreeting

        webTestClient.post().uri("/v1/greetings/200/respond")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("ACCEPTED")
    }

    @Test
    fun `인사 목록 조회 — 200 반환`() {
        setupAuth()
        coEvery { socialClient.listGreetings(10L, null, null) } returns listOf(fakeGreeting)

        webTestClient.get().uri("/v1/greetings")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(200L)
    }

    @Test
    fun `인사 목록 — CB OPEN 시 빈 배열과 X-Fallback 헤더 반환`() {
        setupAuth()
        coEvery {
            cb.executeWithFallback(any(), any(), any(), any<suspend () -> Any?>())
        } coAnswers {
            val onFallback = thirdArg<suspend () -> Unit>()
            onFallback()
            secondArg<Any?>() // emptyList() (fallback 값)
        }

        webTestClient.get().uri("/v1/greetings")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Fallback", "true")
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        webTestClient.get().uri("/v1/greetings")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
