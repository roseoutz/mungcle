package com.mungcle.identity.domain.port.out

import com.mungcle.identity.domain.model.SocialProvider

/**
 * 소셜 로그인 프로바이더 아웃바운드 포트 (Strategy 패턴).
 * 각 소셜 프로바이더 어댑터가 이 인터페이스를 구현한다.
 */
interface SocialAuthPort {
    /** 이 어댑터가 처리하는 프로바이더 */
    val provider: SocialProvider

    /** 액세스 토큰(또는 ID 토큰)으로 프로바이더의 사용자 고유 ID를 조회한다 */
    suspend fun getUserId(accessToken: String): String
}
