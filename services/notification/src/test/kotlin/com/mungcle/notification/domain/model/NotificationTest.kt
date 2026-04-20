package com.mungcle.notification.domain.model

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationTest {

    @Test
    fun `새 알림은 읽지 않은 상태`() {
        val notification = Notification(
            userId = 1L,
            type = NotificationType.GREETING_RECEIVED,
        )
        assertFalse(notification.isRead())
        assertNull(notification.readAt)
    }

    @Test
    fun `markRead 호출 시 readAt 설정`() {
        val notification = Notification(
            userId = 1L,
            type = NotificationType.GREETING_RECEIVED,
        )
        val now = Instant.now()
        val marked = notification.markRead(now)

        assertTrue(marked.isRead())
        assertNotNull(marked.readAt)
        assertEquals(now, marked.readAt)
    }

    @Test
    fun `markRead는 원본을 변경하지 않음`() {
        val notification = Notification(
            userId = 1L,
            type = NotificationType.MESSAGE_RECEIVED,
        )
        val now = Instant.now()
        notification.markRead(now)

        assertFalse(notification.isRead())
    }

    @Test
    fun `이미 읽은 알림도 markRead 가능`() {
        val notification = Notification(
            userId = 1L,
            type = NotificationType.WALK_EXPIRED,
            readAt = Instant.now().minusSeconds(60),
        )
        val now = Instant.now()
        val remarked = notification.markRead(now)

        assertTrue(remarked.isRead())
        assertEquals(now, remarked.readAt)
    }

    @Test
    fun `payload가 빈 맵이 기본값`() {
        val notification = Notification(
            userId = 1L,
            type = NotificationType.GREETING_ACCEPTED,
        )
        assertTrue(notification.payload.isEmpty())
    }

    @Test
    fun `payload에 값 설정 가능`() {
        val payload = mapOf("greetingId" to "123", "fromUserId" to "456")
        val notification = Notification(
            userId = 1L,
            type = NotificationType.GREETING_RECEIVED,
            payload = payload,
        )
        assertEquals("123", notification.payload["greetingId"])
        assertEquals("456", notification.payload["fromUserId"])
    }
}
