package com.mungcle.gateway.infrastructure.resilience

import com.mungcle.gateway.config.RateLimitProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 사용자/IP 기반 Rate Limiting WebFilter.
 * - 인증된 사용자: userId 기준 100 req/min (기본값)
 * - 미인증 사용자: IP 기준 20 req/min (기본값)
 * - 초과 시 429 Too Many Requests + Retry-After 헤더 반환
 */
@Component
@Order(-10) // SecurityWebFiltersOrder.AUTHENTICATION(200) 이후, 비즈니스 필터보다 앞
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitFilter(
    private val props: RateLimitProperties,
) : WebFilter {

    // key -> RateLimitBucket (요청 카운터 + 윈도우 시작 시각)
    private val buckets = ConcurrentHashMap<String, RateLimitBucket>()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .map { ctx -> ctx.authentication?.principal as? Long }
            .defaultIfEmpty(NO_AUTH_SENTINEL)
            .flatMap { principal ->
                val isAuthenticated = principal != NO_AUTH_SENTINEL && principal != null
                val (key, config) = if (isAuthenticated) {
                    "user:$principal" to props.authenticated
                } else {
                    "ip:${resolveClientIp(exchange)}" to props.anonymous
                }

                val bucket = buckets.computeIfAbsent(key) { RateLimitBucket(config.limit, config.duration.toMillis()) }
                val result = bucket.tryConsume()

                val response = exchange.response
                response.headers["X-RateLimit-Limit"] = result.limit.toString()
                response.headers["X-RateLimit-Remaining"] = result.remaining.toString()
                response.headers["X-RateLimit-Reset"] = result.resetInSeconds.toString()

                if (result.allowed) {
                    chain.filter(exchange)
                } else {
                    // 429 Too Many Requests
                    response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                    response.headers.contentType = MediaType.APPLICATION_JSON
                    response.headers["Retry-After"] = result.resetInSeconds.toString()
                    val body = """{"statusCode":429,"code":"RATE_LIMIT_EXCEEDED","message":"요청 한도를 초과했습니다. 잠시 후 다시 시도하세요"}"""
                    val buffer = response.bufferFactory().wrap(body.toByteArray())
                    response.writeWith(Mono.just(buffer))
                }
            }
    }

    private fun resolveClientIp(exchange: ServerWebExchange): String {
        val forwarded = exchange.request.headers.getFirst("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            // X-Forwarded-For 는 "client, proxy1, proxy2" 형태 — 첫 번째가 실제 클라이언트
            return forwarded.split(",").first().trim()
        }
        return exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    companion object {
        // Kotlin에서 Long? defaultIfEmpty sentinel — Long.MIN_VALUE를 "미인증" 표시용으로 사용
        private val NO_AUTH_SENTINEL: Long = Long.MIN_VALUE
    }
}

/**
 * 고정 윈도우(Fixed Window) 방식 토큰 버킷.
 * ConcurrentHashMap에 저장되며 스레드 안전하게 구현.
 */
class RateLimitBucket(
    private val maxRequests: Int,
    private val windowMs: Long,
) {
    private val counter = AtomicInteger(0)

    @Volatile
    private var windowStart: Long = Instant.now().toEpochMilli()

    /**
     * 요청 하나를 소비하고 결과를 반환한다.
     * 윈도우가 만료됐으면 카운터를 리셋한다.
     */
    @Synchronized
    fun tryConsume(): RateLimitResult {
        val now = Instant.now().toEpochMilli()
        if (now - windowStart >= windowMs) {
            // 새 윈도우 시작
            counter.set(0)
            windowStart = now
        }

        val current = counter.incrementAndGet()
        val resetInSeconds = ((windowStart + windowMs - now) / 1000).coerceAtLeast(0)
        val remaining = (maxRequests - current).coerceAtLeast(0)

        return RateLimitResult(
            allowed = current <= maxRequests,
            limit = maxRequests,
            remaining = remaining,
            resetInSeconds = resetInSeconds,
        )
    }
}

data class RateLimitResult(
    val allowed: Boolean,
    val limit: Int,
    val remaining: Int,
    val resetInSeconds: Long,
)
