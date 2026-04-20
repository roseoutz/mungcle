package com.mungcle.identity.domain.port.out

/**
 * 카카오 API 아웃바운드 포트.
 */
interface KakaoApiPort {
    /** 카카오 액세스 토큰으로 사용자 정보를 조회하여 stable user ID 반환 */
    suspend fun getUserId(accessToken: String): String
}
