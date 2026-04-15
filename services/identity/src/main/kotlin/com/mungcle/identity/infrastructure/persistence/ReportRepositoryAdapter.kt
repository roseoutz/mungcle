package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.Report
import com.mungcle.identity.domain.port.out.ReportRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryAdapter(
    private val springDataRepository: ReportSpringDataRepository,
) : ReportRepositoryPort {

    override fun save(report: Report): Report {
        val entity = ReportMapper.toEntity(report)
        val saved = springDataRepository.save(entity)
        return ReportMapper.toDomain(saved)
    }

    override fun countByReportedId(reportedId: Long): Long =
        springDataRepository.countByReportedId(reportedId)
}
