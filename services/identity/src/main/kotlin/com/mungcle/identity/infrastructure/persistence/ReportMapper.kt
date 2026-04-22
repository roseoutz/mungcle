package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.Report

object ReportMapper {
    fun toDomain(entity: ReportEntity): Report = Report(
        id = entity.id,
        reporterId = entity.reporterId,
        reportedId = entity.reportedId,
        reason = entity.reason,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Report): ReportEntity = ReportEntity(
        id = domain.id,
        reporterId = domain.reporterId,
        reportedId = domain.reportedId,
        reason = domain.reason,
        createdAt = domain.createdAt,
    )
}
