package com.mungcle.identity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ReportSpringDataRepository : JpaRepository<ReportEntity, Long> {
    fun countByReportedId(reportedId: Long): Long
}
