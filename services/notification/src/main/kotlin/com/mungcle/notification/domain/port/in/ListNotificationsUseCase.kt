package com.mungcle.notification.domain.port.`in`

import com.mungcle.notification.domain.model.Notification

interface ListNotificationsUseCase {
    data class Query(
        val userId: Long,
        val cursor: Long?,
        val limit: Int,
    )

    data class Result(
        val notifications: List<Notification>,
        val nextCursor: Long?,
    )

    /** cursor 기반 페이지네이션으로 알림 목록을 조회한다 */
    fun execute(query: Query): Result
}
