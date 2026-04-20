package com.mungcle.notification.config

import com.mungcle.notification.domain.port.out.IdentityPort
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import com.mungcle.notification.infrastructure.grpc.client.IdentityGrpcClient
import com.mungcle.notification.infrastructure.persistence.NotificationRepositoryAdapter
import com.mungcle.notification.infrastructure.push.LoggingPushSenderAdapter
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import io.grpc.Channel
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NotificationConfig {

    @Bean
    fun notificationRepositoryPort(adapter: NotificationRepositoryAdapter): NotificationRepositoryPort = adapter

    @Bean
    fun pushSenderPort(adapter: LoggingPushSenderAdapter): PushSenderPort = adapter

    @Bean
    fun identityCoroutineStub(@GrpcClient("identity") channel: Channel): IdentityServiceGrpcKt.IdentityServiceCoroutineStub =
        IdentityServiceGrpcKt.IdentityServiceCoroutineStub(channel)

    @Bean
    fun identityPort(client: IdentityGrpcClient): IdentityPort = client
}
