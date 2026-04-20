package com.mungcle.identity.domain.model

import com.mungcle.identity.domain.exception.InvalidReportReasonException
import com.mungcle.identity.domain.exception.ReportSelfException
import java.time.Instant

/**
 * 신고 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Report(
    val id: Long = 0,
    val reporterId: Long,
    val reportedId: Long,
    val reason: String,
    val createdAt: Instant = Instant.now()
) {
    init {
        if (reporterId == reportedId) throw ReportSelfException()
        if (reason.length !in 1..500) throw InvalidReportReasonException()
    }
}
