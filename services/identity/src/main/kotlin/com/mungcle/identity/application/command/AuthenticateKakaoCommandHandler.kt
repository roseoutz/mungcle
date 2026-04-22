package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.port.`in`.AuthenticateKakaoUseCase
import com.mungcle.identity.domain.port.`in`.AuthenticateSocialUseCase
import org.springframework.stereotype.Service

/**
 * 카카오 로그인 핸들러 — 하위 호환을 위해 유지.
 * 내부적으로 AuthenticateSocialUseCase에 위임한다.
 */
@Service
class AuthenticateKakaoCommandHandler(
    private val authenticateSocial: AuthenticateSocialUseCase,
) : AuthenticateKakaoUseCase {

    override suspend fun execute(command: AuthenticateKakaoUseCase.Command): AuthResult =
        authenticateSocial.execute(
            AuthenticateSocialUseCase.Command(
                provider = SocialProvider.KAKAO,
                accessToken = command.kakaoAccessToken,
            )
        )
}
