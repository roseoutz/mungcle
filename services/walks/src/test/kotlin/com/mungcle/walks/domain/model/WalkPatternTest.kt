package com.mungcle.walks.domain.model

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WalkPatternTest {

    private val baseTime = Instant.parse("2026-01-01T09:00:00Z")

    private fun createPattern(
        id: Long = 1L,
        walkCount: Int = 0,
        lastWalkedAt: Instant = baseTime,
    ) = WalkPattern(
        id = id,
        gridCell = "10:20",
        hourOfDay = 9,
        dogId = 100L,
        walkCount = walkCount,
        lastWalkedAt = lastWalkedAt,
    )

    // ─── incrementCount ──────────────────────────────────────────────────────

    @Test
    fun `incrementCount — walkCount가 1 증가`() {
        val pattern = createPattern(walkCount = 3)
        val walkedAt = baseTime.plusSeconds(3600)
        pattern.incrementCount(walkedAt)
        assertEquals(4, pattern.walkCount)
    }

    @Test
    fun `incrementCount — lastWalkedAt이 전달된 시각으로 갱신`() {
        val pattern = createPattern(walkCount = 0, lastWalkedAt = baseTime)
        val walkedAt = baseTime.plusSeconds(7200)
        pattern.incrementCount(walkedAt)
        assertEquals(walkedAt, pattern.lastWalkedAt)
    }

    @Test
    fun `incrementCount 여러 번 — 누적 횟수가 정확히 반영`() {
        val pattern = createPattern(walkCount = 0)
        pattern.incrementCount(baseTime.plusSeconds(1000))
        pattern.incrementCount(baseTime.plusSeconds(2000))
        pattern.incrementCount(baseTime.plusSeconds(3000))
        assertEquals(3, pattern.walkCount)
        // 마지막 호출 시각이 lastWalkedAt에 반영
        assertEquals(baseTime.plusSeconds(3000), pattern.lastWalkedAt)
    }

    // ─── equals / hashCode ──────────────────────────────────────────────────

    @Test
    fun `같은 id를 가진 WalkPattern은 동등`() {
        val p1 = createPattern(id = 5L, walkCount = 1)
        val p2 = createPattern(id = 5L, walkCount = 99)
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
    }

    @Test
    fun `id=0 WalkPattern은 자기 자신과만 동등`() {
        val p1 = createPattern(id = 0L)
        val p2 = createPattern(id = 0L)
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        assertFalse(p1 == p2)
        assertTrue(p1 == p1)
    }
}
