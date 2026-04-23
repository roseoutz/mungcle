package com.mungcle.gateway.versioning

import com.mungcle.gateway.api.HealthController
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(HealthController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class, ApiVersionFilter::class)
class ApiVersionFilterTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @Test
    fun `버전 헤더 없음 — LATEST 버전 반환`() {
        webTestClient.get().uri("/v1/health")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(ApiVersionFilter.VERSION_HEADER, ApiVersion.LATEST.date.toString())
    }

    @Test
    fun `유효한 버전 날짜 — 해당 버전으로 응답`() {
        webTestClient.get().uri("/v1/health")
            .header(ApiVersionFilter.VERSION_HEADER, "2025-01-01")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(ApiVersionFilter.VERSION_HEADER, ApiVersion.V1.date.toString())
    }

    @Test
    fun `출시 전 날짜 — LATEST 버전으로 fallback`() {
        webTestClient.get().uri("/v1/health")
            .header(ApiVersionFilter.VERSION_HEADER, "2020-01-01")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(ApiVersionFilter.VERSION_HEADER, ApiVersion.LATEST.date.toString())
    }
}
