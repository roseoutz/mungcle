package com.mungcle.gateway.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateGreetingRequest
import com.mungcle.gateway.dto.RespondGreetingRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.SocialClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.social.v1.GreetingStatus
import com.mungcle.proto.social.v1.greetingInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GreetingController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class GreetingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var socialClient: SocialClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

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

        mockMvc.perform(
            post("/api/greetings")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(200L))
            .andExpect(jsonPath("$.status").value("PENDING"))
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

        mockMvc.perform(
            post("/api/greetings/200/respond")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACCEPTED"))
    }

    @Test
    fun `인사 목록 조회 — 200 반환`() {
        setupAuth()
        coEvery { socialClient.listGreetings(10L, null, null) } returns listOf(fakeGreeting)

        mockMvc.perform(
            get("/api/greetings")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(200L))
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        mockMvc.perform(get("/api/greetings"))
            .andExpect(status().isUnauthorized)
    }
}
