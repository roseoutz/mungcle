package com.mungcle.identity.domain.port.out

import com.mungcle.identity.domain.model.Report

/**
 * 신고 저장소 아웃바운드 포트.
 */
interface ReportRepositoryPort {
    /** 신고 저장 */
    fun save(report: Report): Report

    /** 피신고자 ID로 신고 건수 조회 */
    fun countByReportedId(reportedId: Long): Long
}
