package com.mungcle.identity.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ReportTest {

    @Test
    fun `자기 자신 신고 시 예외 발생`() {
        assertThrows<IllegalArgumentException> {
            Report(reporterId = 1L, reportedId = 1L, reason = "테스트")
        }
    }

    @Test
    fun `신고 사유 빈 문자열이면 예외 발생`() {
        assertThrows<IllegalArgumentException> {
            Report(reporterId = 1L, reportedId = 2L, reason = "")
        }
    }

    @Test
    fun `신고 사유 500자 초과 시 예외 발생`() {
        val longReason = "a".repeat(501)
        assertThrows<IllegalArgumentException> {
            Report(reporterId = 1L, reportedId = 2L, reason = longReason)
        }
    }

    @Test
    fun `정상 신고 생성 성공`() {
        val report = Report(reporterId = 1L, reportedId = 2L, reason = "부적절한 행동")
        assertEquals(1L, report.reporterId)
        assertEquals(2L, report.reportedId)
        assertEquals("부적절한 행동", report.reason)
    }

    @Test
    fun `신고 사유 정확히 500자면 성공`() {
        val maxReason = "a".repeat(500)
        val report = Report(reporterId = 1L, reportedId = 2L, reason = maxReason)
        assertEquals(500, report.reason.length)
    }

    @Test
    fun `신고 사유 정확히 1자면 성공`() {
        val report = Report(reporterId = 1L, reportedId = 2L, reason = "a")
        assertEquals(1, report.reason.length)
    }
}
