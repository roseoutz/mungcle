package com.mungcle.identity.domain.port.out

/**
 * 비밀번호 해싱/검증 아웃바운드 포트.
 */
interface PasswordPort {
    /** 평문 비밀번호를 해시 */
    fun hash(raw: String): String

    /** 평문과 해시 비교 */
    fun verify(raw: String, hashed: String): Boolean
}
