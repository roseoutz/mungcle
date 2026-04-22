package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.UpdateUserUseCase
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateUserCommandHandler(
    private val userRepository: UserRepositoryPort,
) : UpdateUserUseCase {

    @Transactional
    override suspend fun execute(command: UpdateUserUseCase.Command): User {
        val user = userRepository.findById(command.userId)
            ?: throw UserNotFoundException(command.userId)

        command.nickname?.let { user.changeNickname(it) }
        command.neighborhood?.let { user.changeNeighborhood(it) }
        command.profilePhotoPath?.let { user.changeProfilePhoto(it) }
        return userRepository.save(user)
    }
}
