package com.mungcle.notification.domain.port.`in`

interface MarkReadUseCase {
    data class Command(
        val notificationId: Long,
        val userId: Long,
    )

    /** 알림 하나를 읽음 처리한다 */
    fun execute(command: Command)
}
