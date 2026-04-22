package com.mungcle.gateway.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class KakaoLoginRequest(
    @field:NotBlank val kakaoAccessToken: String,
)

data class SocialLoginRequest(
    @field:NotBlank val provider: String,     // KAKAO, NAVER, APPLE, GOOGLE
    @field:NotBlank val accessToken: String,
)

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:Size(min = 8) val password: String,
    @field:NotBlank @field:Size(max = 20) val nickname: String,
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

data class PushTokenRequest(
    @field:NotBlank val pushToken: String,
)

data class AuthResponseDto(
    val accessToken: String,
    val user: UserResponse,
)

data class UserResponse(
    val id: Long,
    val nickname: String,
    val neighborhood: String,
    val profilePhotoUrl: String,
)

data class UserDetailResponse(
    val id: Long,
    val nickname: String,
    val neighborhood: String,
    val profilePhotoUrl: String,
    val dogCount: Int,
)

data class UpdateUserRequest(
    val nickname: String?,
    val neighborhood: String?,
    val profilePhotoPath: String?,
)
