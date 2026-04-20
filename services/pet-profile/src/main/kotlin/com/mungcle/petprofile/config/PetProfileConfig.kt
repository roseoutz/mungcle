package com.mungcle.petprofile.config

import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import com.mungcle.petprofile.infrastructure.persistence.DogRepositoryAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PetProfileConfig {

    @Bean
    fun dogRepositoryPort(adapter: DogRepositoryAdapter): DogRepositoryPort = adapter
}
