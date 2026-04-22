package com.mungcle.gateway.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.KakaoLoginRequest
import com.mungcle.gateway.dto.LoginRequest
import com.mungcle.gateway.dto.RegisterRequest
import com.mungcle.gateway.dto.SocialLoginRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.authResponse
import com.mungcle.proto.identity.v1.userInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var identityClient: IdentityClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val fakeAuthResponse = authResponse {
        accessToken = "test-jwt-token"
        user = userInfo {
            id = 1L
            nickname = "testuser"
            neighborhood = "강남구"
            profilePhotoUrl = ""
        }
    }

    @Test
    fun `이메일 회원가입 성공 — 200 반환`() {
        val req = RegisterRequest(email = "test@example.com", password = "password123", nickname = "testuser")
        coEvery { identityClient.registerEmail(any(), any(), any()) } returns fakeAuthResponse

        mockMvc.perform(
            post("/api/auth/email/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
            .andExpect(jsonPath("$.user.nickname").value("testuser"))
    }

    @Test
    fun `이메일 로그인 성공 — 200 반환`() {
        val req = LoginRequest(email = "test@example.com", password = "password123")
        coEvery { identityClient.loginEmail(any(), any()) } returns fakeAuthResponse

        mockMvc.perform(
            post("/api/auth/email/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
    }

    @Test
    fun `이메일 형식 오류 — 400 반환`() {
        val req = mapOf("email" to "not-an-email", "password" to "password123", "nickname" to "testuser")

        mockMvc.perform(
            post("/api/auth/email/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `비밀번호 8자 미만 — 400 반환`() {
        val req = mapOf("email" to "test@example.com", "password" to "short", "nickname" to "testuser")

        mockMvc.perform(
            post("/api/auth/email/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `소셜 로그인 성공 — POST social 200 반환`() {
        val req = SocialLoginRequest(provider = "KAKAO", accessToken = "kakao-token-xyz")
        coEvery { identityClient.authenticateSocial("KAKAO", "kakao-token-xyz") } returns fakeAuthResponse

        mockMvc.perform(
            post("/api/auth/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
            .andExpect(jsonPath("$.user.nickname").value("testuser"))
    }

    @Test
    fun `소셜 로그인 provider 누락 — 400 반환`() {
        val req = mapOf("accessToken" to "some-token")

        mockMvc.perform(
            post("/api/auth/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `소셜 로그인 accessToken 누락 — 400 반환`() {
        val req = mapOf("provider" to "KAKAO")

        mockMvc.perform(
            post("/api/auth/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `카카오 하위 호환 엔드포인트 — POST kakao 200 반환`() {
        val req = KakaoLoginRequest(kakaoAccessToken = "kakao-token-xyz")
        coEvery { identityClient.authenticateSocial("KAKAO", "kakao-token-xyz") } returns fakeAuthResponse

        mockMvc.perform(
            post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("test-jwt-token"))
    }
}
