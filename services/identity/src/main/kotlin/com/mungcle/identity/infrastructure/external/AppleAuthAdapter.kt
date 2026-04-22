package com.mungcle.identity.infrastructure.external

import com.mungcle.identity.domain.exception.SocialAuthFailedException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.port.out.SocialAuthPort
import org.springframework.stereotype.Component

@Component
class AppleAuthAdapter : SocialAuthPort {

    override val provider = SocialProvider.APPLE

    override suspend fun getUserId(accessToken: String): String {
        // Apple Sign In은 ID Token의 서명 검증(JWKS)이 필수.
        // 서명 검증 구현 전까지 비활성화 — 후속 PR에서 구현 예정.
        throw SocialAuthFailedException(provider, "Apple 로그인은 준비 중입니다")
    }
}
