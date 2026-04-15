package com.mungcle.identity.application.query

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.GetUserUseCase
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetUserQueryHandler(
    private val userRepository: UserRepositoryPort,
) : GetUserUseCase {

    override suspend fun execute(userId: Long): User =
        userRepository.findById(userId) ?: throw UserNotFoundException(userId)
}
