package com.mungcle.gateway.infrastructure.resilience

import com.mungcle.gateway.config.RateLimitProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

class RateLimitFilterTest {

    private val props = RateLimitProperties(
        authenticated = RateLimitProperties.LimitConfig(limit = 3, duration = Duration.ofMinutes(1)),
        anonymous = RateLimitProperties.LimitConfig(limit = 2, duration = Duration.ofMinutes(1)),
    )

    private lateinit var filter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        // 테스트마다 새 필터 인스턴스 — 버킷 상태 초기화
        filter = RateLimitFilter(props)
    }

    // -----------------------------------------------
    // 인증된 사용자 — 한도 이내
    // -----------------------------------------------

    @Test
    fun `인증된 사용자가 한도 이내 요청 — 통과(429 아님)`() {
        val exchange = authenticatedExchange(userId = 1L)
        val chain = passThroughChain()

        val result = filter.filter(exchange, chain)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken(1L)))

        StepVerifier.create(result).verifyComplete()

        // MockServerWebExchange는 chain이 status를 설정하지 않으면 null — 429가 아니면 통과
        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
        assertEquals("3", exchange.response.headers.getFirst("X-RateLimit-Limit"))
        assertNotNull(exchange.response.headers.getFirst("X-RateLimit-Remaining"))
        assertNotNull(exchange.response.headers.getFirst("X-RateLimit-Reset"))
    }

    // -----------------------------------------------
    // 인증된 사용자 — 한도 초과
    // -----------------------------------------------

    @Test
    fun `인증된 사용자가 한도 초과 요청 — 429 반환 및 Retry-After 헤더 포함`() {
        // limit=3 이므로 4번째 요청에서 429
        val chain = passThroughChain()
        repeat(3) {
            val ex = authenticatedExchange(userId = 2L)
            filter.filter(ex, chain)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken(2L)))
                .block()
        }

        val exchange = authenticatedExchange(userId = 2L)
        val result = filter.filter(exchange, chain)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken(2L)))

        StepVerifier.create(result).verifyComplete()

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.response.statusCode)
        assertNotNull(exchange.response.headers.getFirst("Retry-After"))
        assertEquals("3", exchange.response.headers.getFirst("X-RateLimit-Limit"))
        assertEquals("0", exchange.response.headers.getFirst("X-RateLimit-Remaining"))
    }

    // -----------------------------------------------
    // 미인증 사용자 — 한도 이내
    // -----------------------------------------------

    @Test
    fun `미인증 사용자가 한도 이내 요청 — 통과(429 아님)`() {
        val exchange = anonymousExchange(ip = "10.0.0.1")
        val chain = passThroughChain()

        // SecurityContext 없이 실행
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        // MockServerWebExchange는 chain이 status를 설정하지 않으면 null — 429가 아니면 통과
        assert(exchange.response.statusCode != HttpStatus.TOO_MANY_REQUESTS)
        assertEquals("2", exchange.response.headers.getFirst("X-RateLimit-Limit"))
    }

    // -----------------------------------------------
    // 미인증 사용자 — 한도 초과
    // -----------------------------------------------

    @Test
    fun `미인증 사용자가 한도 초과 요청 — 429 반환`() {
        // limit=2 이므로 3번째 요청에서 429
        val chain = passThroughChain()
        repeat(2) {
            val ex = anonymousExchange(ip = "10.0.0.2")
            filter.filter(ex, chain).block()
        }

        val exchange = anonymousExchange(ip = "10.0.0.2")
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.response.statusCode)
        assertNotNull(exchange.response.headers.getFirst("Retry-After"))
    }

    // -----------------------------------------------
    // Rate Limit 헤더 항상 존재
    // -----------------------------------------------

    @Test
    fun `Rate Limit 헤더가 모든 응답에 포함된다`() {
        val exchange = anonymousExchange(ip = "10.0.0.3")
        val chain = passThroughChain()

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        assertNotNull(exchange.response.headers.getFirst("X-RateLimit-Limit"))
        assertNotNull(exchange.response.headers.getFirst("X-RateLimit-Remaining"))
        assertNotNull(exchange.response.headers.getFirst("X-RateLimit-Reset"))
    }

    // -----------------------------------------------
    // X-Forwarded-For 헤더 IP 추출
    // -----------------------------------------------

    @Test
    fun `X-Forwarded-For 헤더가 있으면 마지막 IP를 키로 사용한다`() {
        // 스푸핑 방지: "client, proxy1, proxy2" 형태에서 가장 마지막(신뢰 가능 프록시 추가) IP 사용
        val exchange1 = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "1.2.3.4, 10.0.0.1")
                .build()
        )
        val exchange2 = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "5.6.7.8, 10.0.0.1")
                .build()
        )
        val chain = passThroughChain()

        // limit=2이므로 같은 마지막 IP(10.0.0.1)로 두 번 요청
        filter.filter(exchange1, chain).block()
        filter.filter(exchange2, chain).block()

        val exchange3 = MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "9.9.9.9, 10.0.0.1")
                .build()
        )
        StepVerifier.create(filter.filter(exchange3, chain)).verifyComplete()

        // 마지막 IP(10.0.0.1)가 동일하므로 3번째 요청 → 429
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange3.response.statusCode)
    }

    // -----------------------------------------------
    // 헬퍼 함수
    // -----------------------------------------------

    private fun authenticatedExchange(userId: Long): MockServerWebExchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/test").build())

    private fun anonymousExchange(ip: String): MockServerWebExchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", ip)
                .build()
        )

    private fun passThroughChain(): WebFilterChain = WebFilterChain { Mono.empty() }

    private fun authToken(userId: Long) =
        UsernamePasswordAuthenticationToken(userId, null, emptyList())
}
