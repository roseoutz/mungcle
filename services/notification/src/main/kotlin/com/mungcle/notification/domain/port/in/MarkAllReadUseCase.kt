package com.mungcle.notification.domain.port.`in`

interface MarkAllReadUseCase {
    data class Command(val userId: Long)

    /** 사용자의 모든 알림을 읽음 처리한다 */
    fun execute(command: Command)
}
