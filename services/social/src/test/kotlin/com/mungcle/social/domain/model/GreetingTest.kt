package com.mungcle.social.domain.model

import com.mungcle.social.domain.exception.GreetingNotPendingException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GreetingTest {

    private fun pendingGreeting(
        id: Long = 1L,
        expiresAt: Instant = Instant.now().plusSeconds(300),
    ) = Greeting(
        id = id,
        senderUserId = 10L,
        senderDogId = 100L,
        receiverUserId = 20L,
        receiverDogId = 200L,
        receiverWalkId = 300L,
        status = GreetingStatus.PENDING,
        expiresAt = expiresAt,
    )

    @Test
    fun `isPending — PENDING 상태이면 true`() {
        assertTrue(pendingGreeting().isPending())
    }

    @Test
    fun `isPending — ACCEPTED 상태이면 false`() {
        val greeting = pendingGreeting().accept(Instant.now())
        assertFalse(greeting.isPending())
    }

    @Test
    fun `isAccepted — accept 후 true`() {
        val greeting = pendingGreeting().accept(Instant.now())
        assertTrue(greeting.isAccepted())
    }

    @Test
    fun `accept — PENDING에서 ACCEPTED로 상태 전이`() {
        val now = Instant.now()
        val accepted = pendingGreeting().accept(now)

        assertEquals(GreetingStatus.ACCEPTED, accepted.status)
        assertEquals(now, accepted.respondedAt)
    }

    @Test
    fun `accept — expiresAt은 respondedAt으로부터 30분 후`() {
        val now = Instant.now()
        val accepted = pendingGreeting().accept(now)

        val diff = Duration.between(now, accepted.expiresAt)
        assertEquals(1800, diff.seconds)
    }

    @Test
    fun `accept — PENDING이 아니면 GreetingNotPendingException`() {
        val accepted = pendingGreeting().accept(Instant.now())

        assertThrows<GreetingNotPendingException> {
            accepted.accept(Instant.now())
        }
    }

    @Test
    fun `expire — EXPIRED 상태로 전이`() {
        val expired = pendingGreeting().expire()
        assertEquals(GreetingStatus.EXPIRED, expired.status)
    }

    @Test
    fun `expire — EXPIRED 상태이면 GreetingNotPendingException`() {
        val expired = pendingGreeting().expire()

        assertThrows<GreetingNotPendingException> {
            expired.expire()
        }
    }

    @Test
    fun `expire — ACCEPTED 상태에서도 EXPIRED로 전이`() {
        val accepted = pendingGreeting().accept(Instant.now())
        val expired = accepted.expire()
        assertEquals(GreetingStatus.EXPIRED, expired.status)
    }

    @Test
    fun `isExpired — expiresAt 이후이면 true`() {
        val past = Instant.now().minusSeconds(1)
        val greeting = pendingGreeting(expiresAt = past)
        assertTrue(greeting.isExpired(Instant.now()))
    }

    @Test
    fun `isExpired — expiresAt 이전이면 false`() {
        val future = Instant.now().plusSeconds(300)
        val greeting = pendingGreeting(expiresAt = future)
        assertFalse(greeting.isExpired(Instant.now()))
    }

    @Test
    fun `canSendMessage — ACCEPTED이고 미만료이면 true`() {
        val now = Instant.now()
        val accepted = pendingGreeting(expiresAt = now.plusSeconds(1800)).accept(now)
        assertTrue(accepted.canSendMessage(now.plusSeconds(60)))
    }

    @Test
    fun `canSendMessage — ACCEPTED이지만 만료이면 false`() {
        val now = Instant.now()
        val accepted = pendingGreeting(expiresAt = now.plusSeconds(1800)).accept(now)
        assertFalse(accepted.canSendMessage(now.plusSeconds(1801)))
    }

    @Test
    fun `canSendMessage — PENDING이면 false`() {
        val greeting = pendingGreeting()
        assertFalse(greeting.canSendMessage(Instant.now()))
    }
}
