package com.mungcle.identity.config

import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.KakaoApiPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import com.mungcle.identity.infrastructure.external.KakaoApiAdapter
import com.mungcle.identity.infrastructure.persistence.UserRepositoryAdapter
import com.mungcle.identity.infrastructure.security.BcryptPasswordAdapter
import com.mungcle.identity.infrastructure.security.JwtAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class IdentityConfig {

    @Bean
    fun userRepositoryPort(adapter: UserRepositoryAdapter): UserRepositoryPort = adapter

    @Bean
    fun jwtPort(adapter: JwtAdapter): JwtPort = adapter

    @Bean
    fun passwordPort(adapter: BcryptPasswordAdapter): PasswordPort = adapter

    @Bean
    fun kakaoApiPort(adapter: KakaoApiAdapter): KakaoApiPort = adapter

    @Bean
    fun webClient(): WebClient = WebClient.create()
}
