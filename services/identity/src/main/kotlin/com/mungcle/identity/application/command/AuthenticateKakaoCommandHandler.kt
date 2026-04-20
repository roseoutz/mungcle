package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.AuthenticateKakaoUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.KakaoApiPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticateKakaoCommandHandler(
    private val userRepository: UserRepositoryPort,
    private val jwtPort: JwtPort,
    private val kakaoApiPort: KakaoApiPort,
) : AuthenticateKakaoUseCase {

    @Transactional
    override suspend fun execute(command: AuthenticateKakaoUseCase.Command): AuthResult {
        val kakaoId = kakaoApiPort.getUserId(command.kakaoAccessToken)

        val user = userRepository.findByKakaoId(kakaoId)
            ?: userRepository.save(
                User(
                    kakaoId = kakaoId,
                    nickname = "카카오유저_${kakaoId.takeLast(6)}",
                )
            )

        val token = jwtPort.generateToken(user.id)
        return AuthResult(accessToken = token, user = user)
    }
}
