package com.mungcle.gateway.infrastructure.resilience

import com.mungcle.gateway.config.RateLimitProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
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
 * - 인증 필터(SecurityWebFiltersOrder.AUTHENTICATION = 100) 이후에 실행되어야 userId를 읽을 수 있음
 */
@Component
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitFilter(
    private val props: RateLimitProperties,
) : WebFilter {

    // key -> RateLimitBucket (요청 카운터 + 윈도우 시작 시각)
    private val buckets = ConcurrentHashMap<String, RateLimitBucket>()

    /**
     * 메모리 누수 방지: 만료된 버킷을 매 1분마다 제거한다.
     * 마지막 윈도우 시작 시각 기준으로 두 윈도우 이상 경과한 항목을 삭제.
     */
    @Scheduled(fixedRate = 60_000)
    fun evictExpiredBuckets() {
        val now = Instant.now().toEpochMilli()
        buckets.entries.removeIf { (_, bucket) -> bucket.isExpired(now) }
    }

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
                    response.headers.contentType = MediaType("application", "json", Charsets.UTF_8)
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
            // X-Forwarded-For 는 "client, proxy1, proxy2" 형태.
            // 스푸핑 방지를 위해 가장 마지막 항목(신뢰 가능한 가장 가까운 프록시가 추가한 IP)을 사용.
            return forwarded.split(",").last().trim()
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
 * AtomicInteger CAS만으로 락 없이 스레드 안전하게 구현.
 * 윈도우 리셋은 windowStart AtomicLong CAS로 단 한 스레드만 수행.
 */
class RateLimitBucket(
    private val maxRequests: Int,
    private val windowMs: Long,
) {
    private val counter = AtomicInteger(0)
    private val windowStart = java.util.concurrent.atomic.AtomicLong(Instant.now().toEpochMilli())

    /**
     * 요청 하나를 소비하고 결과를 반환한다.
     * 윈도우가 만료됐으면 CAS로 windowStart를 교체하고 카운터를 리셋한다.
     */
    fun tryConsume(): RateLimitResult {
        val now = Instant.now().toEpochMilli()
        val start = windowStart.get()
        if (now - start >= windowMs) {
            // 윈도우 만료 — CAS로 단 한 스레드만 리셋 수행
            if (windowStart.compareAndSet(start, now)) {
                counter.set(0)
            }
        }

        val current = counter.incrementAndGet()
        val currentStart = windowStart.get()
        val resetInSeconds = ((currentStart + windowMs - now) / 1000).coerceAtLeast(0)
        val remaining = (maxRequests - current).coerceAtLeast(0)

        return RateLimitResult(
            allowed = current <= maxRequests,
            limit = maxRequests,
            remaining = remaining,
            resetInSeconds = resetInSeconds,
        )
    }

    /**
     * 버킷이 만료 상태인지 확인한다 (eviction 판단용).
     * 마지막 윈도우 시작으로부터 2배 이상 경과하면 만료로 간주.
     */
    fun isExpired(now: Long): Boolean = now - windowStart.get() >= windowMs * 2
}

data class RateLimitResult(
    val allowed: Boolean,
    val limit: Int,
    val remaining: Int,
    val resetInSeconds: Long,
)
