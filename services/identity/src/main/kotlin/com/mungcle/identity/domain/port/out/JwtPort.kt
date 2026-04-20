package com.mungcle.identity.domain.port.out

/**
 * JWT 생성/검증 아웃바운드 포트.
 */
interface JwtPort {
    /** userId를 claim으로 갖는 JWT 생성 */
    fun generateToken(userId: Long): String

    /** JWT 검증 후 userId 반환. 유효하지 않거나 만료된 경우 null */
    fun validateToken(token: String): Long?
}
