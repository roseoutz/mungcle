package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.application.dto.AuthResult

/**
 * 이메일 회원가입 유스케이스 포트.
 */
interface RegisterEmailUseCase {
    data class Command(val email: String, val password: String, val nickname: String)

    /** 이메일/비밀번호로 회원가입 후 JWT를 포함한 인증 결과 반환 */
    suspend fun execute(command: Command): AuthResult
}
