package com.mungcle.identity.infrastructure.security

import com.mungcle.identity.domain.port.out.PasswordPort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BcryptPasswordAdapter : PasswordPort {

    private val encoder = BCryptPasswordEncoder()

    override fun hash(raw: String): String = encoder.encode(raw)

    override fun verify(raw: String, hashed: String): Boolean = encoder.matches(raw, hashed)
}
