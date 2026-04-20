package com.mungcle.notification.config

import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import com.mungcle.notification.infrastructure.persistence.NotificationRepositoryAdapter
import com.mungcle.notification.infrastructure.push.LoggingPushSenderAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfig {

    @Bean
    fun notificationRepositoryPort(adapter: NotificationRepositoryAdapter): NotificationRepositoryPort = adapter

    @Bean
    fun pushSenderPort(adapter: LoggingPushSenderAdapter): PushSenderPort = adapter
}
