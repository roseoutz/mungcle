package com.mungcle.gateway.versioning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApiVersionTest {

    @Test
    fun `정확한 버전 날짜 — 해당 버전 반환`() {
        val result = ApiVersion.fromDate("2025-01-01")
        assertEquals(ApiVersion.V1, result)
    }

    @Test
    fun `알려진 버전보다 미래 날짜 — 최신 버전 반환`() {
        val result = ApiVersion.fromDate("2099-12-31")
        assertEquals(ApiVersion.LATEST, result)
    }

    @Test
    fun `알려진 최초 버전보다 이전 날짜 — LATEST fallback`() {
        val result = ApiVersion.fromDate("2020-01-01")
        assertEquals(ApiVersion.LATEST, result)
    }

    @Test
    fun `잘못된 날짜 형식 — LATEST fallback`() {
        assertEquals(ApiVersion.LATEST, ApiVersion.fromDate("not-a-date"))
        assertEquals(ApiVersion.LATEST, ApiVersion.fromDate("2026/04/23"))
        assertEquals(ApiVersion.LATEST, ApiVersion.fromDate(""))
    }

    @Test
    fun `LATEST는 entries 마지막 버전`() {
        assertEquals(ApiVersion.entries.last(), ApiVersion.LATEST)
    }

    @Test
    fun `V1 날짜 값 검증`() {
        assertEquals(LocalDate.of(2025, 1, 1), ApiVersion.V1.date)
    }
}
