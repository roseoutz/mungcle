package com.mungcle.identity.domain.port.`in`

/**
 * 신고 생성 유스케이스 포트.
 */
interface CreateReportUseCase {
    /** reporterId가 reportedId를 reason 사유로 신고 */
    suspend fun execute(reporterId: Long, reportedId: Long, reason: String)
}
