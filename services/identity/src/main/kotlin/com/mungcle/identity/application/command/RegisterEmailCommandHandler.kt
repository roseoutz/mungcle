package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.exception.EmailTakenException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.RegisterEmailUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterEmailCommandHandler(
    private val userRepository: UserRepositoryPort,
    private val jwtPort: JwtPort,
    private val passwordPort: PasswordPort,
) : RegisterEmailUseCase {

    @Transactional
    override suspend fun execute(command: RegisterEmailUseCase.Command): AuthResult {
        val normalizedEmail = User.normalizeEmail(command.email)
        User.validateNickname(command.nickname)

        userRepository.findByEmail(normalizedEmail)?.let {
            throw EmailTakenException(normalizedEmail)
        }

        val passwordHash = passwordPort.hash(command.password)
        val user = userRepository.save(
            User(
                email = normalizedEmail,
                passwordHash = passwordHash,
                nickname = command.nickname,
            )
        )
        val token = jwtPort.generateToken(user.id)
        return AuthResult(accessToken = token, user = user)
    }
}
