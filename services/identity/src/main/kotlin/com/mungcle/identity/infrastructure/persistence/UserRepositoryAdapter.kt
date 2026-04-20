package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val springDataRepository: UserSpringDataRepository,
) : UserRepositoryPort {

    override suspend fun save(user: User): User {
        val entity = UserMapper.toEntity(user)
        val saved = springDataRepository.save(entity)
        return UserMapper.toDomain(saved)
    }

    override suspend fun findById(id: Long): User? =
        springDataRepository.findById(id).orElse(null)?.let(UserMapper::toDomain)

    override suspend fun findByEmail(email: String): User? =
        springDataRepository.findByEmail(email).orElse(null)?.let(UserMapper::toDomain)

    override suspend fun findByKakaoId(kakaoId: String): User? =
        springDataRepository.findByKakaoId(kakaoId).orElse(null)?.let(UserMapper::toDomain)
}
