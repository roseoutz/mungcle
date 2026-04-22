package com.mungcle.identity.infrastructure.external

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mungcle.identity.domain.exception.SocialAuthFailedException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.port.out.SocialAuthPort
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class AppleAuthAdapter : SocialAuthPort {

    override val provider = SocialProvider.APPLE

    override suspend fun getUserId(accessToken: String): String {
        // Apple은 ID Token (JWT)을 직접 디코딩하여 sub 클레임 추출
        val parts = accessToken.split(".")
        if (parts.size != 3) throw SocialAuthFailedException(provider, "유효하지 않은 ID 토큰입니다")

        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        val json = jacksonObjectMapper().readTree(payload)

        return json["sub"]?.asText()
            ?: throw SocialAuthFailedException(provider, "사용자 ID를 가져올 수 없습니다")
        // TODO: Apple 공개키로 서명 검증 추가 (프로덕션 필수)
    }
}
