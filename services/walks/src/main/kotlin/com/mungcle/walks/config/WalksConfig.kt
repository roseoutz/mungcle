package com.mungcle.walks.config

import com.mungcle.walks.domain.port.out.WalkEventPublisherPort
import com.mungcle.walks.domain.port.out.WalkPatternRepositoryPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import com.mungcle.walks.infrastructure.kafka.WalkExpiredEventPublisher
import com.mungcle.walks.infrastructure.persistence.WalkPatternRepositoryAdapter
import com.mungcle.walks.infrastructure.persistence.WalkRepositoryAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class WalksConfig {

    @Bean
    fun walkRepositoryPort(adapter: WalkRepositoryAdapter): WalkRepositoryPort = adapter

    @Bean
    fun walkPatternRepositoryPort(adapter: WalkPatternRepositoryAdapter): WalkPatternRepositoryPort = adapter

    @Bean
    fun walkEventPublisherPort(publisher: WalkExpiredEventPublisher): WalkEventPublisherPort = publisher
}
