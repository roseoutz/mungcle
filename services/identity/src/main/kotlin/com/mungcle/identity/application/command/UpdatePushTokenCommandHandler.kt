package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.port.`in`.UpdatePushTokenUseCase
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdatePushTokenCommandHandler(
    private val userRepository: UserRepositoryPort,
) : UpdatePushTokenUseCase {

    @Transactional
    override suspend fun execute(command: UpdatePushTokenUseCase.Command) {
        val user = userRepository.findById(command.userId)
            ?: throw UserNotFoundException(command.userId)
        user.changePushToken(command.pushToken)
        userRepository.save(user)
    }
}
