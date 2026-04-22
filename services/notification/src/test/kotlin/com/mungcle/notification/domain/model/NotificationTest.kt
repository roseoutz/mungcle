package com.mungcle.notification.domain.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NotificationTest {

    private fun createNotification(
        id: Long = 1L,
        read: Boolean = false,
    ) = Notification(
        id = id,
        userId = 10L,
        type = NotificationType.GREETING_RECEIVED,
        payloadJson = """{"greetingId":42}""",
        read = read,
    )

    // ─── markAsRead ──────────────────────────────────────────────────────────

    @Test
    fun `markAsRead — read가 true로 변경`() {
        val notification = createNotification(read = false)
        assertFalse(notification.read)
        notification.markAsRead()
        assertTrue(notification.read)
    }

    @Test
    fun `markAsRead 중복 호출 — 이미 읽음 상태에서 오류 없이 idempotent 동작`() {
        val notification = createNotification(read = true)
        // 두 번 호출해도 예외 없이 read=true 유지
        notification.markAsRead()
        notification.markAsRead()
        assertTrue(notification.read)
    }

    // ─── equals / hashCode ──────────────────────────────────────────────────

    @Test
    fun `같은 id를 가진 Notification은 동등`() {
        val n1 = createNotification(id = 5L)
        val n2 = createNotification(id = 5L)
        assertEquals(n1, n2)
        assertEquals(n1.hashCode(), n2.hashCode())
    }

    @Test
    fun `다른 id를 가진 Notification은 동등하지 않음`() {
        val n1 = createNotification(id = 1L)
        val n2 = createNotification(id = 2L)
        assertNotEquals(n1, n2)
    }

    @Test
    fun `id=0 Notification은 자기 자신과만 동등`() {
        val n1 = Notification(
            id = 0L,
            userId = 10L,
            type = NotificationType.GREETING_RECEIVED,
            payloadJson = """{"greetingId":42}""",
        )
        val n2 = Notification(
            id = 0L,
            userId = 10L,
            type = NotificationType.GREETING_RECEIVED,
            payloadJson = """{"greetingId":42}""",
        )
        // id=0은 참조 동일성 — 서로 다른 인스턴스는 동등하지 않음
        assertFalse(n1 == n2)
        // 자기 자신과는 동등
        assertTrue(n1 == n1)
    }
}
