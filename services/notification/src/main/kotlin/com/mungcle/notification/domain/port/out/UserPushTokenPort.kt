package com.mungcle.notification.domain.port.out

/**
 * 사용자 푸시 토큰 조회 아웃바운드 포트.
 * identity 서비스에서 pushToken을 가져온다.
 */
interface UserPushTokenPort {
    suspend fun getPushToken(userId: Long): String?
}
