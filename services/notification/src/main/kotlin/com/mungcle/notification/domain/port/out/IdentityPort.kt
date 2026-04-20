package com.mungcle.notification.domain.port.out

/**
 * Identity 서비스 조회 아웃바운드 포트 — pushToken 조회용.
 */
interface IdentityPort {
    suspend fun getPushToken(userId: Long): String?
}
