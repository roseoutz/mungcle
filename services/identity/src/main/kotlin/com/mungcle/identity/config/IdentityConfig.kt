package com.mungcle.identity.config

import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import com.mungcle.identity.infrastructure.persistence.UserRepositoryAdapter
import com.mungcle.identity.infrastructure.security.BcryptPasswordAdapter
import com.mungcle.identity.infrastructure.security.JwtAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IdentityConfig {

    @Bean
    fun userRepositoryPort(adapter: UserRepositoryAdapter): UserRepositoryPort = adapter

    @Bean
    fun jwtPort(adapter: JwtAdapter): JwtPort = adapter

    @Bean
    fun passwordPort(adapter: BcryptPasswordAdapter): PasswordPort = adapter
}
