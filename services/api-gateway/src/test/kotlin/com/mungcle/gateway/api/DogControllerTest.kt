package com.mungcle.gateway.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.CreateDogRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.petprofile.v1.DogSize
import com.mungcle.proto.petprofile.v1.dogInfo
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

@WebMvcTest(DogController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class DogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var petProfileClient: PetProfileClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

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

        mockMvc.perform(
            post("/api/dogs")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("초코"))
    }

    @Test
    fun `반려견 목록 조회 성공 — 200 반환`() {
        setupAuth()
        coEvery { petProfileClient.getDogsByOwner(10L) } returns listOf(fakeDog)

        mockMvc.perform(
            get("/api/dogs")
                .header("Authorization", "Bearer valid-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("초코"))
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        mockMvc.perform(
            get("/api/dogs")
        )
            .andExpect(status().isUnauthorized)
    }
}
