package com.mungcle.gateway.api

import com.mungcle.gateway.dto.AuthResponseDto
import com.mungcle.gateway.dto.KakaoLoginRequest
import com.mungcle.gateway.dto.LoginRequest
import com.mungcle.gateway.dto.PushTokenRequest
import com.mungcle.gateway.dto.RegisterRequest
import com.mungcle.gateway.dto.SocialLoginRequest
import com.mungcle.gateway.dto.UserResponse
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.mungcle.proto.identity.v1.AuthResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(private val identityClient: IdentityClient) {

    // 기존 카카오 엔드포인트 유지 (하위 호환)
    @PostMapping("/kakao")
    suspend fun kakaoLogin(@Valid @RequestBody req: KakaoLoginRequest): AuthResponseDto =
        identityClient.authenticateSocial("KAKAO", req.kakaoAccessToken).toDto()

    // 범용 소셜 로그인 엔드포인트
    @PostMapping("/social")
    suspend fun socialLogin(@Valid @RequestBody req: SocialLoginRequest): AuthResponseDto =
        identityClient.authenticateSocial(req.provider, req.accessToken).toDto()

    @PostMapping("/email/register")
    suspend fun register(@Valid @RequestBody req: RegisterRequest): AuthResponseDto =
        identityClient.registerEmail(req.email, req.password, req.nickname).toDto()

    @PostMapping("/email/login")
    suspend fun login(@Valid @RequestBody req: LoginRequest): AuthResponseDto =
        identityClient.loginEmail(req.email, req.password).toDto()

    @PostMapping("/push-token")
    suspend fun updatePushToken(@AuthUser userId: Long, @Valid @RequestBody req: PushTokenRequest) {
        identityClient.updatePushToken(userId, req.pushToken)
    }

    private fun AuthResponse.toDto() = AuthResponseDto(
        accessToken = accessToken,
        user = UserResponse(
            id = user.id,
            nickname = user.nickname,
            neighborhood = user.neighborhood,
            profilePhotoUrl = user.profilePhotoUrl,
        ),
    )
}
