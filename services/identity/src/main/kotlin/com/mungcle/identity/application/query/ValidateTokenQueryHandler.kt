package com.mungcle.identity.application.query

import com.mungcle.identity.domain.port.`in`.ValidateTokenUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import org.springframework.stereotype.Service

@Service
class ValidateTokenQueryHandler(
    private val jwtPort: JwtPort,
) : ValidateTokenUseCase {

    override suspend fun execute(query: ValidateTokenUseCase.Query): Long? =
        jwtPort.validateToken(query.accessToken)
}
