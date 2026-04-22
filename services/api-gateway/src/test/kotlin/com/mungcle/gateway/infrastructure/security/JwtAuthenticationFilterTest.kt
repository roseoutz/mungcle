package com.mungcle.gateway.infrastructure.security

import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.proto.identity.v1.validateTokenResponse
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import io.mockk.mockk
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest {

    private val identityClient: IdentityClient = mockk()
    private val filter = JwtAuthenticationFilter(identityClient)

    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `유효한 토큰 — SecurityContext에 인증 설정`() {
        val validResponse = validateTokenResponse {
            userId = 42L
            valid = true
        }
        coEvery { identityClient.validateToken("valid-token") } returns validResponse

        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer valid-token")
        }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertEquals(42L, auth?.principal)
    }

    @Test
    fun `토큰 없음 — SecurityContext 비어있음`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `만료된 토큰 — SecurityContext 비어있음`() {
        coEvery { identityClient.validateToken("expired-token") } throws StatusException(Status.UNAUTHENTICATED)

        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer expired-token")
        }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `유효하지 않은 토큰 응답 — SecurityContext 비어있음`() {
        val invalidResponse = validateTokenResponse {
            userId = 0L
            valid = false
        }
        coEvery { identityClient.validateToken("invalid-token") } returns invalidResponse

        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer invalid-token")
        }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
    }
}
