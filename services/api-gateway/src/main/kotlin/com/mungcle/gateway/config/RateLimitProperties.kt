package com.mungcle.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Rate Limiting 설정 — application.yml의 gateway.rate-limit 바인딩
 */
@ConfigurationProperties(prefix = "gateway.rate-limit")
data class RateLimitProperties(
    val authenticated: LimitConfig = LimitConfig(limit = 100, duration = Duration.ofMinutes(1)),
    val anonymous: LimitConfig = LimitConfig(limit = 20, duration = Duration.ofMinutes(1)),
) {
    data class LimitConfig(
        val limit: Int,
        val duration: Duration,
    )
}
