package com.mungcle.identity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserSpringDataRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun findByKakaoId(kakaoId: String): Optional<UserEntity>
}
