package com.mungcle.identity.domain.port.`in`

/**
 * 푸시 토큰 업데이트 유스케이스 포트.
 */
interface UpdatePushTokenUseCase {
    data class Command(val userId: Long, val pushToken: String)

    /** 사용자의 FCM 푸시 토큰 갱신 */
    suspend fun execute(command: Command)
}
