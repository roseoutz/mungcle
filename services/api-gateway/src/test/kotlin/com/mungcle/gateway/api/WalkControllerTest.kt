package com.mungcle.gateway.api

import com.mungcle.gateway.config.SecurityConfig
import com.mungcle.gateway.config.WebConfig
import com.mungcle.gateway.dto.StartWalkRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.grpc.WalksClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.resilience.FallbackResult
import com.mungcle.gateway.infrastructure.security.AuthUserArgumentResolver
import com.mungcle.gateway.infrastructure.security.JwtAuthenticationFilter
import com.mungcle.proto.identity.v1.userInfo
import com.mungcle.proto.identity.v1.validateTokenResponse
import com.mungcle.proto.petprofile.v1.DogSize
import com.mungcle.proto.petprofile.v1.dogInfo
import com.mungcle.proto.walks.v1.WalkStatus
import com.mungcle.proto.walks.v1.WalkType
import com.mungcle.proto.walks.v1.nearbyWalkInfo
import com.mungcle.proto.walks.v1.walkInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(WalkController::class)
@Import(SecurityConfig::class, WebConfig::class, JwtAuthenticationFilter::class, AuthUserArgumentResolver::class)
class WalkControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var walksClient: WalksClient

    @MockkBean
    private lateinit var identityClient: IdentityClient

    @MockkBean
    private lateinit var petProfileClient: PetProfileClient

    @MockkBean
    private lateinit var cb: CircuitBreakerWrapper

    @BeforeEach
    fun setupCb() {
        coEvery { cb.execute(any(), any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>()() }
        coEvery { cb.executeWithFallback(any(), any(), any<suspend () -> Any?>()) } coAnswers {
            FallbackResult(thirdArg<suspend () -> Any?>()(), isFallback = false)
        }
    }

    private val fakeWalkInfo = walkInfo {
        id = 100L
        dogId = 1L
        userId = 10L
        type = WalkType.WALK_TYPE_OPEN
        gridCell = "183750:710850"
        status = WalkStatus.WALK_STATUS_ACTIVE
        startedAt = 1700000000L
        endsAt = 1700003600L
    }

    private val fakeDog = dogInfo {
        id = 1L
        ownerId = 10L
        name = "초코"
        breed = "골든리트리버"
        size = DogSize.DOG_SIZE_LARGE
        temperaments += listOf("FRIENDLY")
        sociability = 4
        photoUrl = ""
        vaccinationRegistered = true
    }

    private val fakeUser = userInfo {
        id = 10L
        nickname = "홍길동"
        neighborhood = "서초동"
        profilePhotoUrl = ""
    }

    private fun setupAuth(userId: Long = 10L) {
        coEvery { identityClient.validateToken(any()) } returns validateTokenResponse {
            this.userId = userId
            valid = true
        }
    }

    @Test
    fun `산책 시작 성공 — 200 반환`() {
        setupAuth()
        val req = StartWalkRequest(dogId = 1L, lat = 37.5, lng = 127.0, open = true)
        coEvery { walksClient.startWalk(any(), any(), any(), any(), any()) } returns fakeWalkInfo

        webTestClient.post().uri("/v1/walks/start")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(100L)
            .jsonPath("$.status").isEqualTo("ACTIVE")
    }

    @Test
    fun `산책 종료 성공 — 200 반환`() {
        setupAuth()
        val endedWalk = walkInfo {
            id = 100L
            dogId = 1L
            userId = 10L
            type = WalkType.WALK_TYPE_OPEN
            gridCell = "183750:710850"
            status = WalkStatus.WALK_STATUS_ENDED
            startedAt = 1700000000L
            endsAt = 1700003600L
        }
        coEvery { walksClient.stopWalk(100L, 10L) } returns endedWalk

        webTestClient.post().uri("/v1/walks/100/stop")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("ENDED")
    }

    @Test
    fun `근처 산책 BFF 조합 — 200 반환`() {
        setupAuth()
        val nearbyWalk = nearbyWalkInfo {
            walkId = 100L
            dogId = 1L
            userId = 10L
            gridDistance = 0
            startedAt = 1700000000L
        }

        coEvery { identityClient.getBlockedUserIds(10L) } returns emptyList()
        coEvery { walksClient.getNearbyWalks(any(), any(), any()) } returns listOf(nearbyWalk)
        coEvery { petProfileClient.getDogsByIds(listOf(1L)) } returns listOf(fakeDog)
        coEvery { identityClient.getUsersByIds(listOf(10L)) } returns listOf(fakeUser)

        webTestClient.get().uri("/v1/walks/nearby?lat=37.5&lng=127.0")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.walks[0].walkId").isEqualTo(100L)
            .jsonPath("$.walks[0].dog.name").isEqualTo("초코")
            .jsonPath("$.walks[0].owner.nickname").isEqualTo("홍길동")
    }

    @Test
    fun `내 활성 산책 조회 — 200 반환`() {
        setupAuth()
        coEvery { walksClient.getMyActiveWalks(10L) } returns listOf(fakeWalkInfo)

        webTestClient.get().uri("/v1/walks/me/active")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(100L)
    }

    @Test
    fun `비인증 접근 — 401 반환`() {
        webTestClient.get().uri("/v1/walks/me/active")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
