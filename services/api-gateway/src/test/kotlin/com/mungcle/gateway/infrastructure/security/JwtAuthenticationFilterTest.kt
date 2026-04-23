package com.mungcle.gateway.infrastructure.security

import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.proto.identity.v1.validateTokenResponse
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class JwtAuthenticationFilterTest {

    private val identityClient: IdentityClient = mockk()
    private val filter = JwtAuthenticationFilter(identityClient)

    @Test
    fun `유효한 토큰 — SecurityContext에 인증 설정`() {
        val validResponse = validateTokenResponse {
            userId = 42L
            valid = true
        }
        coEvery { identityClient.validateToken("valid-token") } returns validResponse

        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("Authorization", "Bearer valid-token")
                .build()
        )

        var capturedPrincipal: Any? = null
        val chain = WebFilterChain { ex ->
            ReactiveSecurityContextHolder.getContext()
                .doOnNext { ctx -> capturedPrincipal = ctx.authentication?.principal }
                .then()
        }

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(42L, capturedPrincipal)
    }

    @Test
    fun `토큰 없음 — 체인 계속 진행`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test").build()
        )

        var chainCalled = false
        val chain = WebFilterChain { _ ->
            chainCalled = true
            Mono.empty()
        }

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(true, chainCalled)
    }

    @Test
    fun `만료된 토큰 — SecurityContext 비어있음`() {
        coEvery { identityClient.validateToken("expired-token") } throws StatusException(Status.UNAUTHENTICATED)

        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("Authorization", "Bearer expired-token")
                .build()
        )

        var authPresent = false
        val chain = WebFilterChain { _ ->
            ReactiveSecurityContextHolder.getContext()
                .doOnNext { ctx -> authPresent = ctx.authentication != null }
                .then(Mono.empty())
        }

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(false, authPresent)
    }

    @Test
    fun `유효하지 않은 토큰 응답 — SecurityContext 비어있음`() {
        val invalidResponse = validateTokenResponse {
            userId = 0L
            valid = false
        }
        coEvery { identityClient.validateToken("invalid-token") } returns invalidResponse

        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("Authorization", "Bearer invalid-token")
                .build()
        )

        var authPresent = false
        val chain = WebFilterChain { _ ->
            ReactiveSecurityContextHolder.getContext()
                .doOnNext { ctx -> authPresent = ctx.authentication != null }
                .then(Mono.empty())
        }

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete()

        assertEquals(false, authPresent)
    }
}
