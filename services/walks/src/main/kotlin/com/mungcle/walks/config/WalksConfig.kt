package com.mungcle.walks.config

import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import com.mungcle.walks.infrastructure.persistence.WalkRepositoryAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WalksConfig {

    @Bean
    fun walkRepositoryPort(adapter: WalkRepositoryAdapter): WalkRepositoryPort = adapter
}
