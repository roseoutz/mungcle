package com.mungcle.identity.domain.model

import com.mungcle.identity.domain.exception.InvalidNicknameException
import java.time.Instant

/**
 * 사용자 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class User(
    val id: Long = 0,
    val kakaoId: String? = null,
    val email: String? = null,
    val passwordHash: String? = null,
    val nickname: String,
    val pushToken: String? = null,
    val createdAt: Instant = Instant.now()
) {
    companion object {
        private val NICKNAME_REGEX = Regex("^[가-힣a-zA-Z0-9_]{2,16}$")

        /** 이메일을 소문자 trim 정규화 */
        fun normalizeEmail(email: String): String = email.trim().lowercase()

        /** 닉네임 유효성 검증. 실패 시 [InvalidNicknameException] 던짐 */
        fun validateNickname(nickname: String) {
            if (!NICKNAME_REGEX.matches(nickname)) throw InvalidNicknameException(nickname)
        }
    }
}
