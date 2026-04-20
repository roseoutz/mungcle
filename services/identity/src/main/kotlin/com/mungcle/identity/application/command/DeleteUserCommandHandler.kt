package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.port.`in`.DeleteUserUseCase
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteUserCommandHandler(
    private val userRepository: UserRepositoryPort,
) : DeleteUserUseCase {

    @Transactional
    override suspend fun execute(userId: Long) {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException(userId)
        userRepository.save(user.softDelete())
    }
}
