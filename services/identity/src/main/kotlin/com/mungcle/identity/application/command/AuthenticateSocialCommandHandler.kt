package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.exception.UnsupportedProviderException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.AuthenticateSocialUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.SocialAuthPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticateSocialCommandHandler(
    socialAuthAdapters: List<SocialAuthPort>,
    private val userRepository: UserRepositoryPort,
    private val jwtPort: JwtPort,
) : AuthenticateSocialUseCase {

    // Strategy 디스패치 맵: provider → adapter
    private val adapterMap: Map<SocialProvider, SocialAuthPort> =
        socialAuthAdapters.associateBy { it.provider }

    @Transactional
    override suspend fun execute(command: AuthenticateSocialUseCase.Command): AuthResult {
        val adapter = adapterMap[command.provider]
            ?: throw UnsupportedProviderException(command.provider)

        val socialId = adapter.getUserId(command.accessToken)

        val user = userRepository.findBySocialProviderAndSocialId(command.provider, socialId)
            ?: userRepository.save(
                User(
                    socialProvider = command.provider,
                    socialId = socialId,
                    nickname = "${command.provider.name.lowercase()}유저_${socialId.takeLast(6)}",
                )
            )

        val token = jwtPort.generateToken(user.id)
        return AuthResult(accessToken = token, user = user)
    }
}
