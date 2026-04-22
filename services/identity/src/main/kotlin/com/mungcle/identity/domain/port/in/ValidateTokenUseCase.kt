package com.mungcle.identity.domain.port.`in`

/**
 * JWT 토큰 검증 유스케이스 포트.
 */
interface ValidateTokenUseCase {
    data class Query(val accessToken: String)

    /** JWT를 검증하고 userId 반환. 유효하지 않으면 null */
    suspend fun execute(query: Query): Long?
}
