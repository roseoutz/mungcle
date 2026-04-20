package com.mungcle.identity.infrastructure.external

import com.mungcle.identity.domain.port.out.KakaoApiPort
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class KakaoApiAdapter(
    private val webClient: WebClient,
) : KakaoApiPort {

    override suspend fun getUserId(accessToken: String): String {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .awaitBody<Map<String, Any>>()

        return response["id"]?.toString()
            ?: throw IllegalStateException("카카오 API에서 사용자 ID를 가져올 수 없습니다")
    }
}
