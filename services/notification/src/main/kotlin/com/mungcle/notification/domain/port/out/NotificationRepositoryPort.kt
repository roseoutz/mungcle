package com.mungcle.notification.domain.port.out

import com.mungcle.notification.domain.model.Notification

interface NotificationRepositoryPort {
    fun save(notification: Notification): Notification
    fun findById(id: Long): Notification?

    /**
     * cursor 기반 페이지네이션으로 사용자의 알림을 최신순으로 조회한다.
     * cursor가 null이면 첫 페이지.
     */
    fun findByUserId(userId: Long, cursor: Long?, limit: Int): List<Notification>

    /** 사용자의 모든 미읽음 알림을 읽음 처리한다 */
    fun markAllReadByUserId(userId: Long)

    /** 단일 알림을 읽음 처리한다 */
    fun markRead(id: Long)
}
