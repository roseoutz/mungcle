package com.mungcle.identity.application.query

import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.GetUsersByIdsUseCase
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetUsersByIdsQueryHandler(
    private val userRepository: UserRepositoryPort,
) : GetUsersByIdsUseCase {

    override suspend fun execute(userIds: List<Long>): List<User> =
        userRepository.findByIds(userIds)
}
