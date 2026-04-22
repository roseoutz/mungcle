package com.mungcle.identity.infrastructure.external

import com.mungcle.identity.domain.exception.SocialAuthFailedException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.port.out.SocialAuthPort
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class NaverAuthAdapter(
    private val webClient: WebClient,
) : SocialAuthPort {

    override val provider = SocialProvider.NAVER

    override suspend fun getUserId(accessToken: String): String {
        val response = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<Map<String, Any>>()

        @Suppress("UNCHECKED_CAST")
        val responseMap = response["response"] as? Map<String, Any>
            ?: throw SocialAuthFailedException(provider, "네이버 API 응답 형식 오류")

        return responseMap["id"]?.toString()
            ?: throw SocialAuthFailedException(provider, "사용자 ID를 가져올 수 없습니다")
    }
}
