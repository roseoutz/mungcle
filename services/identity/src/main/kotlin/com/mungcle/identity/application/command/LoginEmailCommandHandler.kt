package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.exception.InvalidCredentialsException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.LoginEmailUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginEmailCommandHandler(
    private val userRepository: UserRepositoryPort,
    private val jwtPort: JwtPort,
    private val passwordPort: PasswordPort,
) : LoginEmailUseCase {

    @Transactional(readOnly = true)
    override suspend fun execute(command: LoginEmailUseCase.Command): AuthResult {
        val normalizedEmail = User.normalizeEmail(command.email)
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw InvalidCredentialsException()

        val hashed = user.passwordHash ?: throw InvalidCredentialsException()
        if (!passwordPort.verify(command.password, hashed)) {
            throw InvalidCredentialsException()
        }

        val token = jwtPort.generateToken(user.id)
        return AuthResult(accessToken = token, user = user)
    }
}
