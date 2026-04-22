package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.model.SocialProvider

/**
 * 범용 소셜 로그인 유스케이스 포트.
 */
interface AuthenticateSocialUseCase {
    data class Command(val provider: SocialProvider, val accessToken: String)

    /** 소셜 액세스 토큰으로 인증 — 신규 사용자면 자동 가입 후 JWT 반환 */
    suspend fun execute(command: Command): AuthResult
}
