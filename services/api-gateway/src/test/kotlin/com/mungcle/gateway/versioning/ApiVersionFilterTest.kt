package com.mungcle.gateway.versioning

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.mungcle.gateway.api.HealthController

/**
 * ApiVersionFilter 통합 테스트.
 * HealthController(/v1/health)를 사용해 필터 동작을 검증한다.
 */
@WebMvcTest(HealthController::class)
@Import(
    SecurityConfig::class,
    WebConfig::class,
    JwtAuthenticationFilter::class,
    AuthUserArgumentResolver::class,
    ApiVersionFilter::class,
)
class ApiVersionFilterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var identityClient: IdentityClient

    // ─── 버전 헤더 해석 ────────────────────────────────────────────────────────

    @Test
    fun `유효한 날짜 헤더 — 응답에 해당 버전 echo`() {
        mockMvc.perform(
            get("/v1/health")
                .header(ApiVersionFilter.VERSION_HEADER, "2026-04-23")
        )
            .andExpect(status().isOk)
            .andExpect(header().string(ApiVersionFilter.VERSION_HEADER, "2026-04-23"))
    }

    @Test
    fun `헤더 없음 — LATEST 버전으로 fallback`() {
        mockMvc.perform(get("/v1/health"))
            .andExpect(status().isOk)
            .andExpect(header().string(ApiVersionFilter.VERSION_HEADER, ApiVersion.LATEST.date.toString()))
    }

    @Test
    fun `잘못된 날짜 형식 — LATEST 버전으로 fallback`() {
        mockMvc.perform(
            get("/v1/health")
                .header(ApiVersionFilter.VERSION_HEADER, "invalid-date")
        )
            .andExpect(status().isOk)
            .andExpect(header().string(ApiVersionFilter.VERSION_HEADER, ApiVersion.LATEST.date.toString()))
    }

    @Test
    fun `미래 날짜 — LATEST 버전으로 해석`() {
        mockMvc.perform(
            get("/v1/health")
                .header(ApiVersionFilter.VERSION_HEADER, "2099-01-01")
        )
            .andExpect(status().isOk)
            .andExpect(header().string(ApiVersionFilter.VERSION_HEADER, ApiVersion.LATEST.date.toString()))
    }

    // ─── URL 라우팅 ────────────────────────────────────────────────────────────

    @Test
    fun `v1 prefix URL — 정상 라우팅`() {
        mockMvc.perform(get("/v1/health"))
            .andExpect(status().isOk)
    }
}
