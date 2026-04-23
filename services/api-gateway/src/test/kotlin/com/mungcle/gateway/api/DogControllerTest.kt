package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateDogRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.resilience.ServiceUnavailableException
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.petprofile.v1.DogSize
import com.mungcle.proto.petprofile.v1.dogInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(DogController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class DogControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var petProfileClient: PetProfileClient

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

    private val fakeDog = dogInfo {
        id = 1L
        ownerId = 10L
        name = "초코"
        breed = "골든리트리버"
        size = DogSize.DOG_SIZE_LARGE
        temperaments += listOf("FRIENDLY")
        sociability = 4
        photoUrl = ""
        vaccinationRegistered = false
    }

    private fun setupAuth(userId: Long = 10L) {
        val tokenResponse = validateTokenResponse {
            this.userId = userId
            valid = true
        }
        coEvery { identityClient.validateToken(any()) } returns tokenResponse
    }

    @Test
    fun `반려견 등록 성공 — 201 반환`() {
        setupAuth()
        val req = CreateDogRequest(name = "초코", breed = "골든리트리버", size = "LARGE", sociability = 4)
        coEvery { petProfileClient.createDog(any(), any(), any(), any(), any(), any(), null, null) } returns fakeDog

        webTestClient.post().uri("/v1/dogs")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.name").isEqualTo("초코")
    }

    @Test
    fun `반려견 목록 조회 성공 — 200 반환`() {
        setupAuth()
        coEvery { petProfileClient.getDogsByOwner(10L) } returns listOf(fakeDog)

        webTestClient.get().uri("/v1/dogs")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("초코")
    }

    @Test
    fun `반려견 목록 — CB OPEN 시 빈 배열과 X-Fallback 헤더 반환`() {
        setupAuth()
        // CB OPEN 시 executeWithFallback이 fallback(emptyList)을 반환하고 onFallback 콜백을 호출함
        coEvery {
            cb.executeWithFallback(any(), any(), any(), any<suspend () -> Any?>())
        } coAnswers {
            val onFallback = thirdArg<suspend () -> Unit>()
            onFallback()
            secondArg<Any?>() // fallback 값 반환
        }

        webTestClient.get().uri("/v1/dogs")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Fallback", "true")
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `반려견 단건 조회 — CB OPEN 시 503 반환`() {
        setupAuth()
        coEvery { cb.execute(any(), any<suspend () -> Any?>()) } throws
            ServiceUnavailableException("pet-profile-service", io.github.resilience4j.circuitbreaker.CallNotPermittedException.createCallNotPermittedException(
                io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.ofDefaults().circuitBreaker("test").also {
                    it.transitionToOpenState()
                }
            ))

        webTestClient.get().uri("/v1/dogs/1")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isEqualTo(503)
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        webTestClient.get().uri("/v1/dogs")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
