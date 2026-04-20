package com.mungcle.identity.infrastructure.security

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JwtAdapterTest {

    private val secret = "mungcle-test-secret-key-must-be-at-least-32-bytes"
    private val expiration = 3600_000L // 1시간

    private val adapter = JwtAdapter(secret = secret, expiration = expiration)

    @Test
    fun `토큰 생성 후 검증 라운드트립`() {
        val userId = 42L
        val token = adapter.generateToken(userId)
        val result = adapter.validateToken(token)
        assertEquals(userId, result)
    }

    @Test
    fun `다른 userId로 생성된 토큰 구분`() {
        val token1 = adapter.generateToken(1L)
        val token2 = adapter.generateToken(2L)
        assertEquals(1L, adapter.validateToken(token1))
        assertEquals(2L, adapter.validateToken(token2))
    }

    @Test
    fun `만료된 토큰은 null 반환`() {
        val expiredAdapter = JwtAdapter(secret = secret, expiration = -1L) // 이미 만료
        val token = expiredAdapter.generateToken(99L)
        val result = adapter.validateToken(token)
        assertNull(result)
    }

    @Test
    fun `잘못된 형식의 토큰은 null 반환`() {
        val result = adapter.validateToken("not.a.valid.jwt.token")
        assertNull(result)
    }

    @Test
    fun `빈 문자열 토큰은 null 반환`() {
        val result = adapter.validateToken("")
        assertNull(result)
    }

    @Test
    fun `변조된 토큰은 null 반환`() {
        val token = adapter.generateToken(1L)
        val tampered = token.dropLast(5) + "XXXXX"
        val result = adapter.validateToken(tampered)
        assertNull(result)
    }
}
