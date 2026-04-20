package com.mungcle.identity.infrastructure.security

import com.mungcle.identity.domain.port.out.JwtPort
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtAdapter(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) : JwtPort {

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    override fun generateToken(userId: Long): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date(now))
            .expiration(Date(now + expiration))
            .signWith(signingKey)
            .compact()
    }

    override fun validateToken(token: String): Long? =
        try {
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.subject.toLongOrNull()
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
}
