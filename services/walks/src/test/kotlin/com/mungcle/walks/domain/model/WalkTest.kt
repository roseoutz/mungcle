package com.mungcle.walks.domain.model

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WalkTest {

    private val now = Instant.now()

    private fun createWalk(
        type: WalkType = WalkType.OPEN,
        status: WalkStatus = WalkStatus.ACTIVE,
        endsAt: Instant = now.plus(Duration.ofMinutes(60)),
    ) = Walk(
        id = 1L,
        dogId = 100L,
        userId = 200L,
        type = type,
        gridCell = GridCell("10:20"),
        status = status,
        startedAt = now,
        endsAt = endsAt,
    )

    @Test
    fun `isExpired - 만료 시간 지나면 true`() {
        val walk = createWalk(endsAt = now.minus(Duration.ofMinutes(1)))
        assertTrue(walk.isExpired(now))
    }

    @Test
    fun `isExpired - 만료 시간 전이면 false`() {
        val walk = createWalk(endsAt = now.plus(Duration.ofMinutes(30)))
        assertFalse(walk.isExpired(now))
    }

    @Test
    fun `isOpen - OPEN 타입이면 true`() {
        val walk = createWalk(type = WalkType.OPEN)
        assertTrue(walk.isOpen())
    }

    @Test
    fun `isOpen - SOLO 타입이면 false`() {
        val walk = createWalk(type = WalkType.SOLO)
        assertFalse(walk.isOpen())
    }

    @Test
    fun `end - 상태가 ENDED로 변경`() {
        val walk = createWalk()
        val ended = walk.end(now)
        assertEquals(WalkStatus.ENDED, ended.status)
    }

    @Test
    fun `end - 다른 필드는 변경되지 않음`() {
        val walk = createWalk()
        val ended = walk.end(now)
        assertEquals(walk.id, ended.id)
        assertEquals(walk.dogId, ended.dogId)
        assertEquals(walk.userId, ended.userId)
        assertEquals(walk.type, ended.type)
        assertEquals(walk.gridCell, ended.gridCell)
    }
}
