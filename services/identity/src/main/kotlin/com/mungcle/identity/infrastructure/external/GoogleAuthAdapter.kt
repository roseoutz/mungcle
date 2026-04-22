package com.mungcle.identity.infrastructure.external

import com.mungcle.identity.domain.exception.SocialAuthFailedException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.port.out.SocialAuthPort
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class GoogleAuthAdapter(
    private val webClient: WebClient,
) : SocialAuthPort {

    override val provider = SocialProvider.GOOGLE

    override suspend fun getUserId(accessToken: String): String {
        try {
            val response = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .awaitBody<Map<String, Any>>()

            return response["sub"]?.toString()
                ?: throw SocialAuthFailedException(provider, "사용자 ID를 가져올 수 없습니다")
        } catch (e: SocialAuthFailedException) {
            throw e
        } catch (e: Exception) {
            throw SocialAuthFailedException(provider, "인증 서버 통신 실패")
        }
    }
}
