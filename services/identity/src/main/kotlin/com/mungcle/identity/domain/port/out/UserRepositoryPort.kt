package com.mungcle.identity.domain.port.out

import com.mungcle.identity.domain.model.User

/**
 * 사용자 저장소 아웃바운드 포트.
 */
interface UserRepositoryPort {
    /** 사용자 저장 (신규 생성 또는 업데이트) */
    suspend fun save(user: User): User

    /** ID로 사용자 조회 */
    suspend fun findById(id: Long): User?

    /** 이메일로 사용자 조회 */
    suspend fun findByEmail(email: String): User?

    /** 카카오 ID로 사용자 조회 */
    suspend fun findByKakaoId(kakaoId: String): User?
}
