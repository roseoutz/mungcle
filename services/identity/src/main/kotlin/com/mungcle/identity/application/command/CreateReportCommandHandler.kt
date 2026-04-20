package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.model.Report
import com.mungcle.identity.domain.port.`in`.CreateReportUseCase
import com.mungcle.identity.domain.port.out.ReportRepositoryPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val FLAG_THRESHOLD = 3L

@Service
class CreateReportCommandHandler(
    private val reportRepository: ReportRepositoryPort,
    private val userRepository: UserRepositoryPort,
) : CreateReportUseCase {

    @Transactional
    override suspend fun execute(reporterId: Long, reportedId: Long, reason: String) {
        reportRepository.save(
            Report(reporterId = reporterId, reportedId = reportedId, reason = reason)
        )

        val count = reportRepository.countByReportedId(reportedId)
        if (count >= FLAG_THRESHOLD) {
            val reported = userRepository.findById(reportedId)
                ?: throw UserNotFoundException(reportedId)
            if (!reported.flaggedForReview) {
                userRepository.save(reported.copy(flaggedForReview = true))
            }
        }
    }
}
