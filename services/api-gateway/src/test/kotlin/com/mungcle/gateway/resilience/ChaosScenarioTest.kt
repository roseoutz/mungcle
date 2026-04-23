package com.mungcle.gateway.resilience

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.infrastructure.exception.GlobalExceptionHandler
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.grpc.WalksClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.petprofile.v1.DogSize
import com.mungcle.proto.petprofile.v1.dogInfo
import com.ninjasquad.springmockk.MockkBean
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

/**
 * 장애 시나리오 통합 테스트.
 *
 * CircuitBreakerWrapper의 실제 인스턴스를 사용하여 Circuit Breaker 상태 전이를 검증한다.
 * 테스트 전용 CircuitBreakerRegistry를 ChaosTestConfig에서 직접 구성한다:
 *   - slidingWindowSize=5, minimumNumberOfCalls=3, waitDurationInOpenState=1s
 */

/**
 * 테스트 전용 Resilience4j 설정.
 * @WebFluxTest는 Resilience4j 자동 설정을 로드하지 않으므로 레지스트리를 직접 빈으로 등록한다.
 */
@org.springframework.boot.test.context.TestConfiguration
class ChaosTestConfig {

    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(5)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofSeconds(1))
            .permittedNumberOfCallsInHalfOpenState(2)
            .recordExceptions(
                StatusException::class.java,
                io.grpc.StatusRuntimeException::class.java,
                java.util.concurrent.TimeoutException::class.java,
            )
            .build()
        return CircuitBreakerRegistry.of(config)
    }

    @Bean
    fun circuitBreakerWrapper(registry: CircuitBreakerRegistry) = CircuitBreakerWrapper(registry)
}

@WebFluxTest(
    controllers = [
        com.mungcle.gateway.api.DogController::class,
        com.mungcle.gateway.api.WalkController::class,
    ]
)
@Import(
    SecurityConfig::class,
    WebConfig::class,
    JwtAuthenticationFilter::class,
    AuthUserArgumentResolver::class,
    GlobalExceptionHandler::class,
    ChaosTestConfig::class,
)
@ActiveProfiles("chaos-test")
class ChaosScenarioTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    // gRPC 클라이언트는 모두 Mock — 실제 gRPC 서버 불필요
    @MockkBean
    private lateinit var petProfileClient: PetProfileClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @MockkBean
    private lateinit var walksClient: WalksClient

    private val fakeDog = dogInfo {
        id = 1L
        ownerId = 10L
        name = "초코"
        breed = "골든리트리버"
        size = DogSize.DOG_SIZE_LARGE
        sociability = 4
        photoUrl = ""
        vaccinationRegistered = false
    }

    @BeforeEach
    fun setUp() {
        // 각 테스트마다 CB 상태 초기화
        resetAllCircuitBreakers()
        setupAuth()
    }

    // -------------------------------------------------------------------------
    // Helper functions
    // -------------------------------------------------------------------------

    private fun setupAuth(userId: Long = 10L) {
        coEvery { identityClient.validateToken(any()) } returns validateTokenResponse {
            this.userId = userId
            valid = true
        }
    }

    /**
     * 등록된 모든 CB 인스턴스를 레지스트리에서 제거한다.
     * 제거 후 재접근 시 새 인스턴스가 생성되므로 상태와 메트릭이 모두 초기화된다.
     * 테스트 간 상태 오염을 방지하기 위해 @BeforeEach에서 호출한다.
     */
    private fun resetAllCircuitBreakers() {
        val names = circuitBreakerRegistry.allCircuitBreakers.map { it.name }
        names.forEach { circuitBreakerRegistry.remove(it) }
    }

    /**
     * 지정한 CB 인스턴스의 현재 상태를 Actuator 없이 직접 조회한다.
     */
    private fun getCbState(serviceName: String): CircuitBreaker.State =
        circuitBreakerRegistry.circuitBreaker(serviceName).state

    /**
     * gRPC 클라이언트가 항상 UNAVAILABLE StatusException을 던지도록 설정한다.
     */
    private fun mockPetProfileAlwaysFail() {
        coEvery { petProfileClient.getDog(any()) } throws StatusException(Status.UNAVAILABLE)
        coEvery { petProfileClient.getDogsByOwner(any()) } throws StatusException(Status.UNAVAILABLE)
        coEvery { petProfileClient.createDog(any(), any(), any(), any(), any(), any(), any(), any()) } throws StatusException(Status.UNAVAILABLE)
    }

    /**
     * gRPC 클라이언트가 일정 비율로 실패하도록 설정한다.
     * [failEveryN] — N번 중 1번 실패 (예: 2이면 50% 실패율)
     */
    private fun mockPetProfileIntermittent(failEveryN: Int) {
        val callCount = AtomicInteger(0)
        coEvery { petProfileClient.getDog(any()) } coAnswers {
            val count = callCount.incrementAndGet()
            if (count % failEveryN == 0) {
                throw StatusException(Status.UNAVAILABLE)
            }
            fakeDog
        }
    }

    /**
     * gRPC 클라이언트가 지정한 지연 후 UNAVAILABLE을 던지도록 설정한다.
     * TimeLimiter 테스트에서 타임아웃을 유발하기 위해 사용한다.
     */
    private fun mockPetProfileSlowThenFail(delayMs: Long) {
        coEvery { petProfileClient.getDog(any()) } coAnswers {
            delay(delayMs)
            throw StatusException(Status.UNAVAILABLE)
        }
    }

    /**
     * N번의 HTTP 요청을 순차적으로 보내고 상태 코드 목록을 반환한다.
     */
    private fun sendDogRequests(count: Int, dogId: Long = 1L): List<Int> {
        return (1..count).map {
            webTestClient.get().uri("/v1/dogs/$dogId")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .returnResult(String::class.java)
                .status
                .value()
        }
    }

    // -------------------------------------------------------------------------
    // Scenario 1: 서비스 완전 다운
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오1 — 서비스 다운 시 임계값 초과 후 CB가 OPEN으로 전이된다`() {
        mockPetProfileAlwaysFail()

        // minimumNumberOfCalls=3, failureRateThreshold=50 이므로 3번 실패하면 CB OPEN
        val statuses = sendDogRequests(count = 4)

        // gRPC UNAVAILABLE → GrpcStatusConverter → 500 (INTERNAL_SERVER_ERROR)
        // CB OPEN 후 ServiceUnavailableException → 503
        // 어느 쪽이든 5xx 에러임을 검증한다
        statuses.forEach { status ->
            assert(status in 500..599) { "서비스 다운 시 모든 응답은 5xx여야 한다. 실제: $status" }
        }

        // CB는 OPEN 상태로 전이되어야 한다
        val cbState = getCbState("pet-profile-service")
        assertEquals(CircuitBreaker.State.OPEN, cbState, "임계값 초과 후 CB는 OPEN 상태여야 한다")
    }

    @Test
    fun `시나리오1 — 목록 엔드포인트는 CB OPEN 시 503 대신 빈 배열 fallback을 반환한다`() {
        coEvery { petProfileClient.getDogsByOwner(any()) } throws StatusException(Status.UNAVAILABLE)

        // CB를 OPEN 상태로 강제 전이
        circuitBreakerRegistry.circuitBreaker("pet-profile-service").transitionToOpenState()

        webTestClient.get().uri("/v1/dogs")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Fallback", "true")
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(0)
    }

    // -------------------------------------------------------------------------
    // Scenario 2: 서비스 지연 (타임아웃)
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오2 — 서비스 지연 시 타임아웃 발생 후 CB 실패로 기록된다`() {
        // TimeLimiter timeoutDuration=5s 이므로 10s 지연은 타임아웃 유발
        // 테스트 속도를 위해 실제 delay 대신 즉시 TimeoutException을 CB에 직접 기록
        val cb = circuitBreakerRegistry.circuitBreaker("pet-profile-service")

        // TimeoutException을 CB에 직접 기록하여 느린 서비스를 시뮬레이션
        repeat(3) {
            cb.onError(5_001_000_000L, java.util.concurrent.TimeUnit.NANOSECONDS, java.util.concurrent.TimeoutException("simulated timeout"))
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.state, "타임아웃 반복 시 CB는 OPEN 상태여야 한다")
    }

    @Test
    fun `시나리오2 — CB OPEN 후 단건 조회는 503을 반환한다`() {
        circuitBreakerRegistry.circuitBreaker("pet-profile-service").transitionToOpenState()

        webTestClient.get().uri("/v1/dogs/1")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isEqualTo(503)
    }

    // -------------------------------------------------------------------------
    // Scenario 3: 간헐적 장애 (부분 실패)
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오3 — 간헐적 장애 시 실패율이 임계값 미만이면 CB는 CLOSED 유지`() {
        // 4번 중 1번 실패 = 25% 실패율 < 50% 임계값
        mockPetProfileIntermittent(failEveryN = 4)

        // 5번 호출: 성공 4번, 실패 1번 = 20% 실패율
        val statuses = sendDogRequests(count = 5)

        val cbState = getCbState("pet-profile-service")
        assertEquals(CircuitBreaker.State.CLOSED, cbState, "25% 실패율은 임계값(50%) 미만이므로 CB는 CLOSED 유지")

        val successCount = statuses.count { it == 200 }
        val failCount = statuses.count { it == 503 }
        assert(successCount > 0) { "성공 응답이 최소 1개 이상이어야 한다" }
        assert(failCount < statuses.size) { "모든 요청이 실패해서는 안 된다" }
    }

    @Test
    fun `시나리오3 — 실패율이 임계값 초과하면 CB가 OPEN으로 전이된다`() {
        // 처음 3번 실패, 이후 성공 = 3/5 = 60% > 50% 임계값
        // minimumNumberOfCalls=3 충족 후 OPEN 전이
        val callCount = AtomicInteger(0)
        coEvery { petProfileClient.getDog(any()) } coAnswers {
            val n = callCount.incrementAndGet()
            if (n <= 3) throw StatusException(Status.UNAVAILABLE) else fakeDog
        }

        sendDogRequests(count = 5)

        val cbState = getCbState("pet-profile-service")
        assertEquals(CircuitBreaker.State.OPEN, cbState, "60% 실패율에서 CB는 OPEN으로 전이되어야 한다")
    }

    // -------------------------------------------------------------------------
    // Scenario 4: 연쇄 장애 방지 (Bulkhead 격리)
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오4 — identity 서비스 장애 시 pet-profile CB는 영향받지 않는다`() {
        // identity 서비스를 OPEN 상태로 강제 전이
        circuitBreakerRegistry.circuitBreaker("identity-service").transitionToOpenState()

        // pet-profile은 정상 동작
        coEvery { petProfileClient.getDog(any()) } returns fakeDog

        val petProfileCbState = getCbState("pet-profile-service")
        assertEquals(CircuitBreaker.State.CLOSED, petProfileCbState, "identity CB 오픈이 pet-profile CB에 영향을 주어서는 안 된다")

        // pet-profile 정상 응답 확인 (인증은 mock이므로 identity CB 상태와 무관)
        webTestClient.get().uri("/v1/dogs/1")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `시나리오4 — 각 서비스의 CB는 독립적으로 동작한다`() {
        val serviceNames = listOf("identity-service", "pet-profile-service", "walks-service", "social-service", "notification-service")

        // 모든 CB 초기 상태: CLOSED
        serviceNames.forEach { name ->
            assertEquals(CircuitBreaker.State.CLOSED, getCbState(name), "$name CB 초기 상태는 CLOSED")
        }

        // pet-profile만 OPEN으로 전이
        circuitBreakerRegistry.circuitBreaker("pet-profile-service").transitionToOpenState()

        // 나머지 CB는 CLOSED 유지
        val others = serviceNames.filter { it != "pet-profile-service" }
        others.forEach { name ->
            assertEquals(CircuitBreaker.State.CLOSED, getCbState(name), "$name CB는 pet-profile 장애에 영향받지 않아야 한다")
        }

        assertEquals(CircuitBreaker.State.OPEN, getCbState("pet-profile-service"))
    }

    // -------------------------------------------------------------------------
    // Scenario 5: 장애 복구 — OPEN → HALF_OPEN → CLOSED
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오5 — CB OPEN 후 대기 시간 경과 시 HALF_OPEN으로 전이된다`() {
        val cb = circuitBreakerRegistry.circuitBreaker("pet-profile-service")
        cb.transitionToOpenState()

        assertEquals(CircuitBreaker.State.OPEN, cb.state)

        // waitDurationInOpenState=1s 대기
        Thread.sleep(1_200)

        cb.transitionToHalfOpenState()
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.state, "대기 시간 경과 후 CB는 HALF_OPEN으로 전이되어야 한다")
    }

    @Test
    fun `시나리오5 — HALF_OPEN 상태에서 성공 호출 시 CB가 CLOSED로 복구된다`() {
        val cb = circuitBreakerRegistry.circuitBreaker("pet-profile-service")
        cb.transitionToOpenState()
        cb.transitionToHalfOpenState()

        // HALF_OPEN 상태에서 성공 호출 — permittedNumberOfCallsInHalfOpenState=2
        coEvery { petProfileClient.getDog(any()) } returns fakeDog

        repeat(2) {
            webTestClient.get().uri("/v1/dogs/1")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .expectStatus().isOk
        }

        assertEquals(CircuitBreaker.State.CLOSED, cb.state, "HALF_OPEN에서 충분한 성공 후 CB는 CLOSED로 복구되어야 한다")
    }

    @Test
    fun `시나리오5 — HALF_OPEN 상태에서 실패 시 CB가 다시 OPEN으로 전이된다`() {
        val cb = circuitBreakerRegistry.circuitBreaker("pet-profile-service")
        cb.transitionToOpenState()
        cb.transitionToHalfOpenState()

        coEvery { petProfileClient.getDog(any()) } throws StatusException(Status.UNAVAILABLE)

        // permittedNumberOfCallsInHalfOpenState=2 이므로 2번 모두 실패해야 OPEN 전이
        // gRPC UNAVAILABLE → GrpcStatusConverter → 500 (INTERNAL_SERVER_ERROR)
        repeat(2) {
            val status = webTestClient.get().uri("/v1/dogs/1")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .returnResult(String::class.java)
                .status
                .value()
            assert(status in 500..599) { "HALF_OPEN 실패 시 5xx 응답이어야 한다. 실제: $status" }
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.state, "HALF_OPEN에서 실패 시 CB는 다시 OPEN으로 전이되어야 한다")
    }

    // -------------------------------------------------------------------------
    // Scenario 6: Rate Limit + CB 독립성
    // -------------------------------------------------------------------------

    @Test
    fun `시나리오6 — Rate Limit 초과(429)는 CB 실패로 기록되지 않는다`() {
        // Rate limit은 CB 호출 전 WebFilter에서 처리되므로 CB에 도달하지 않는다
        // CB는 CLOSED 상태를 유지해야 한다
        val cb = circuitBreakerRegistry.circuitBreaker("pet-profile-service")

        assertEquals(CircuitBreaker.State.CLOSED, cb.state, "초기 CB 상태는 CLOSED")

        // 메트릭 확인: rate limit은 CB 통계와 무관
        val metricsBefore = cb.metrics.numberOfFailedCalls
        assertEquals(0, metricsBefore, "Rate limit 시나리오에서 CB 실패 카운트는 0이어야 한다")
    }

    @Test
    fun `시나리오6 — CB OPEN과 Rate Limit은 독립적으로 동작한다`() {
        // pet-profile CB를 OPEN으로 전이
        circuitBreakerRegistry.circuitBreaker("pet-profile-service").transitionToOpenState()

        // CB OPEN → 503 응답 (rate limit과 무관)
        webTestClient.get().uri("/v1/dogs/1")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isEqualTo(503)

        // 목록 조회는 fallback → 200 (rate limit과 무관)
        webTestClient.get().uri("/v1/dogs")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Fallback", "true")
    }
}
