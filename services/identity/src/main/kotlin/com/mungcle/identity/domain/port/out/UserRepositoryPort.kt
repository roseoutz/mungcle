package com.mungcle.identity.domain.port.out

import com.mungcle.identity.domain.model.SocialProvider
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

    /** 소셜 프로바이더 + 소셜 ID로 사용자 조회 */
    suspend fun findBySocialProviderAndSocialId(provider: SocialProvider, socialId: String): User?

    /** 여러 ID로 사용자 목록 조회 */
    suspend fun findByIds(ids: List<Long>): List<User>
}
