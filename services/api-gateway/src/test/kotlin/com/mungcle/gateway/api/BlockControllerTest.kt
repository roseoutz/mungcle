package com.mungcle.gateway.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateBlockRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.blockInfo
import com.mungcle.proto.identity.v1.listBlocksResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(BlockController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class BlockControllerTest {

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
    fun `차단 생성 성공 — 201 반환`() {
        setupAuth()
        coJustRun { identityClient.createBlock(any(), any()) }
        val req = CreateBlockRequest(blockedUserId = 99L)

        mockMvc.perform(
            post("/api/blocks")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
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

        mockMvc.perform(
            get("/api/blocks")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].blockedUserId").value(99))
            .andExpect(jsonPath("$[0].blockedNickname").value("blocked_user"))
    }

    @Test
    fun `차단 해제 성공 — 204 반환`() {
        setupAuth()
        coJustRun { identityClient.deleteBlock(any(), any()) }

        mockMvc.perform(
            delete("/api/blocks/99")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `비인증 차단 목록 조회 — 401 반환`() {
        mockMvc.perform(get("/api/blocks"))
            .andExpect(status().isUnauthorized)
    }
}
