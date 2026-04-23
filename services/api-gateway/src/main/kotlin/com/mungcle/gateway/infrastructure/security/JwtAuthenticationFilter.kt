package com.mungcle.gateway.infrastructure.security

import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val identityClient: IdentityClient,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = extractToken(exchange) ?: return chain.filter(exchange)

        return mono {
            try {
                val validateResponse = identityClient.validateToken(token)
                if (validateResponse.valid) {
                    UsernamePasswordAuthenticationToken(validateResponse.userId, null, emptyList())
                } else {
                    null
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                // 인증 실패 — 인증 없이 계속
                null
            }
        }.flatMap { auth ->
            if (auth != null) {
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            } else {
                chain.filter(exchange)
            }
        }
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        val header = exchange.request.headers.getFirst("Authorization") ?: return null
        return if (header.startsWith("Bearer ")) header.substring(7) else null
    }
}
