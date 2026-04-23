package com.mungcle.gateway.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateReportRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coJustRun
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ReportController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class ReportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var identityClient: IdentityClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

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

        mockMvc.perform(
            post("/v1/reports")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `reason 미입력 시 400 반환`() {
        setupAuth()
        val body = """{"reportedUserId": 99, "reason": ""}"""

        mockMvc.perform(
            post("/v1/reports")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `reportedUserId 누락 시 400 반환`() {
        setupAuth()
        val body = """{"reason": "스팸/광고"}"""

        mockMvc.perform(
            post("/v1/reports")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `비인증 신고 요청 — 401 반환`() {
        mockMvc.perform(
            post("/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reportedUserId": 99, "reason": "스팸/광고"}""")
        )
            .andExpect(status().isUnauthorized)
    }
}
