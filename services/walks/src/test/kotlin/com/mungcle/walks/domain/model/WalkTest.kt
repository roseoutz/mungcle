package com.mungcle.walks.domain.model

import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class WalkTest {

    private fun createWalk(
        type: WalkType = WalkType.OPEN,
        status: WalkStatus = WalkStatus.ACTIVE,
        endsAt: Instant = Instant.now().plusSeconds(3600),
    ) = Walk(
        id = 1L,
        dogId = 10L,
        userId = 100L,
        type = type,
        gridCell = "100:200",
        status = status,
        startedAt = Instant.now(),
        endsAt = endsAt,
    )

    @Test
    fun `OPEN 산책은 isOpen true`() {
        val walk = createWalk(type = WalkType.OPEN)
        assertTrue(walk.isOpen())
    }

    @Test
    fun `SOLO 산책은 isOpen false`() {
        val walk = createWalk(type = WalkType.SOLO)
        assertFalse(walk.isOpen())
    }

    @Test
    fun `만료 시간 이전이면 isExpired false`() {
        val walk = createWalk(endsAt = Instant.now().plusSeconds(3600))
        assertFalse(walk.isExpired(Instant.now()))
    }

    @Test
    fun `만료 시간 이후면 isExpired true`() {
        val walk = createWalk(endsAt = Instant.now().minusSeconds(60))
        assertTrue(walk.isExpired(Instant.now()))
    }

    @Test
    fun `ACTIVE 산책 종료 성공`() {
        val walk = createWalk(status = WalkStatus.ACTIVE)
        val ended = walk.end(Instant.now())
        assertEquals(WalkStatus.ENDED, ended.status)
    }

    @Test
    fun `이미 종료된 산책을 다시 종료하면 예외`() {
        val walk = createWalk(status = WalkStatus.ENDED)
        assertThrows<WalkAlreadyEndedException> {
            walk.end(Instant.now())
        }
    }
}
