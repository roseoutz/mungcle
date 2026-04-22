package com.mungcle.social.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageTest {

    private fun createMessage(
        id: Long = 1L,
        body: String = "안녕하세요",
    ) = Message(
        id = id,
        greetingId = 10L,
        senderUserId = 20L,
        body = body,
    )

    // ─── 생성 유효성 ─────────────────────────────────────────────────────────

    @Test
    fun `1자 본문으로 메시지 생성 성공`() {
        val message = createMessage(body = "A")
        assertEquals("A", message.body)
    }

    @Test
    fun `140자 본문으로 메시지 생성 성공`() {
        val body = "a".repeat(140)
        val message = createMessage(body = body)
        assertEquals(140, message.body.length)
    }

    @Test
    fun `빈 본문이면 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            createMessage(body = "")
        }
    }

    @Test
    fun `공백만 있는 본문이면 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            createMessage(body = "   ")
        }
    }

    @Test
    fun `141자 본문이면 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            createMessage(body = "a".repeat(141))
        }
    }

    // ─── equals / hashCode ──────────────────────────────────────────────────

    @Test
    fun `같은 id를 가진 Message는 동등`() {
        val m1 = createMessage(id = 5L)
        val m2 = createMessage(id = 5L)
        assertEquals(m1, m2)
        assertEquals(m1.hashCode(), m2.hashCode())
    }

    @Test
    fun `id=0 Message는 자기 자신과만 동등`() {
        val m1 = createMessage(id = 0L)
        val m2 = createMessage(id = 0L)
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        assertFalse(m1 == m2)
        assertTrue(m1 == m1)
    }
}
