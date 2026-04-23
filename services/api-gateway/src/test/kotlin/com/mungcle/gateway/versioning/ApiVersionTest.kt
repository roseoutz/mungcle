package com.mungcle.gateway.versioning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApiVersionTest {

    // ─── ApiVersion.resolve ───────────────────────────────────────────────────

    @Test
    fun `정확한 버전 날짜 — 해당 버전 반환`() {
        val result = ApiVersion.resolve("2026-04-23")
        assertEquals(ApiVersion.V_2026_04_23, result)
    }

    @Test
    fun `알려진 버전보다 미래 날짜 — LATEST 반환`() {
        val result = ApiVersion.resolve("2099-12-31")
        assertEquals(ApiVersion.LATEST, result)
    }

    @Test
    fun `알려진 최초 버전보다 이전 날짜 — null 반환`() {
        // 출시일보다 이전이면 해당하는 버전이 없음
        val result = ApiVersion.resolve("2020-01-01")
        assertNull(result)
    }

    @Test
    fun `잘못된 날짜 형식 — null 반환`() {
        assertNull(ApiVersion.resolve("not-a-date"))
        assertNull(ApiVersion.resolve("2026/04/23"))
        assertNull(ApiVersion.resolve(""))
    }

    @Test
    fun `LATEST는 entries 마지막 버전`() {
        assertEquals(ApiVersion.entries.last(), ApiVersion.LATEST)
    }

    @Test
    fun `V_2026_04_23 날짜 값 검증`() {
        assertEquals(LocalDate.of(2026, 4, 23), ApiVersion.V_2026_04_23.date)
    }
}
