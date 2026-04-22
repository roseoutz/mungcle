package com.mungcle.gateway.api

import com.mungcle.gateway.dto.AuthResponseDto
import com.mungcle.gateway.dto.KakaoLoginRequest
import com.mungcle.gateway.dto.LoginRequest
import com.mungcle.gateway.dto.PushTokenRequest
import com.mungcle.gateway.dto.RegisterRequest
import com.mungcle.gateway.dto.UserResponse
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.mungcle.proto.identity.v1.AuthResponse
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val identityClient: IdentityClient) {

    @PostMapping("/kakao")
    fun kakaoLogin(@Valid @RequestBody req: KakaoLoginRequest): AuthResponseDto = runBlocking {
        identityClient.authenticateKakao(req.kakaoAccessToken).toDto()
    }

    @PostMapping("/email/register")
    fun register(@Valid @RequestBody req: RegisterRequest): AuthResponseDto = runBlocking {
        identityClient.registerEmail(req.email, req.password, req.nickname).toDto()
    }

    @PostMapping("/email/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponseDto = runBlocking {
        identityClient.loginEmail(req.email, req.password).toDto()
    }

    @PostMapping("/push-token")
    fun updatePushToken(@AuthUser userId: Long, @Valid @RequestBody req: PushTokenRequest): Unit = runBlocking {
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
