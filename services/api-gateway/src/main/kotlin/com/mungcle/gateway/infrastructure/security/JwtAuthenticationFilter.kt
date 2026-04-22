package com.mungcle.gateway.infrastructure.security

import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.proto.identity.v1.ValidateTokenResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val identityClient: IdentityClient,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)
        if (token != null) {
            try {
                val validateResponse: ValidateTokenResponse = runBlocking { identityClient.validateToken(token) }
                if (validateResponse.valid) {
                    val auth = UsernamePasswordAuthenticationToken(validateResponse.userId, null, emptyList())
                    SecurityContextHolder.getContext().authentication = auth
                }
            } catch (e: Exception) {
                // 인증 실패 — SecurityContext 비우고 계속
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}
