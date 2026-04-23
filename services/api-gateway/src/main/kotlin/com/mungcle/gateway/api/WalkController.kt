package com.mungcle.gateway.api

import com.mungcle.common.domain.GridCell
import com.mungcle.gateway.dto.DogCardResponse
import com.mungcle.gateway.dto.NearbyWalkCardResponse
import com.mungcle.gateway.dto.NearbyWalksResponse
import com.mungcle.gateway.dto.OwnerCardResponse
import com.mungcle.gateway.dto.StartWalkRequest
import com.mungcle.gateway.dto.WalkResponse
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.grpc.WalksClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.mungcle.proto.walks.v1.WalkInfo
import com.mungcle.proto.walks.v1.WalkType
import jakarta.validation.Valid
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/v1/walks")
class WalkController(
    private val walksClient: WalksClient,
    private val identityClient: IdentityClient,
    private val petProfileClient: PetProfileClient,
    private val cb: CircuitBreakerWrapper,
) {

    @PostMapping("/start")
    suspend fun startWalk(@AuthUser userId: Long, @Valid @RequestBody req: StartWalkRequest): WalkResponse {
        val walkType = if (req.open) WalkType.WALK_TYPE_OPEN else WalkType.WALK_TYPE_SOLO
        return cb.execute("walks-service") { walksClient.startWalk(userId, req.dogId, walkType, req.lat, req.lng) }.toResponse()
    }

    @PostMapping("/{id}/stop")
    suspend fun stopWalk(@AuthUser userId: Long, @PathVariable id: Long): WalkResponse =
        cb.execute("walks-service") { walksClient.stopWalk(walkId = id, userId = userId) }.toResponse()

    @GetMapping("/nearby")
    suspend fun getNearbyWalks(
        @AuthUser userId: Long,
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        exchange: ServerWebExchange,
    ): NearbyWalksResponse {
        val gridCell = GridCell.fromCoordinates(lat, lng).value

        val (blockedUserIds, nearbyWalksFallback) = coroutineScope {
            val blocked = async {
                cb.executeWithFallback("identity-service", emptyList<Long>()) {
                    identityClient.getBlockedUserIds(userId)
                }
            }
            val walks = async {
                cb.executeWithFallback("walks-service", emptyList()) {
                    walksClient.getNearbyWalks(gridCell, userId, emptyList())
                }
            }
            blocked.await() to walks.await()
        }

        // 어느 한 서비스라도 fallback이면 X-Fallback 헤더 설정
        if (blockedUserIds.isFallback || nearbyWalksFallback.isFallback) {
            exchange.response.headers.add("X-Fallback", "true")
        }

        val filteredWalks = nearbyWalksFallback.value.filter { it.userId !in blockedUserIds.value }

        if (filteredWalks.isEmpty()) return NearbyWalksResponse(emptyList())

        val dogIds = filteredWalks.map { it.dogId }.distinct()
        val userIds = filteredWalks.map { it.userId }.distinct()

        val (dogsFallback, usersFallback) = coroutineScope {
            val dogsDeferred = async {
                cb.executeWithFallback("pet-profile-service", emptyList()) {
                    petProfileClient.getDogsByIds(dogIds)
                }
            }
            val usersDeferred = async {
                cb.executeWithFallback("identity-service", emptyList()) {
                    identityClient.getUsersByIds(userIds)
                }
            }
            dogsDeferred.await() to usersDeferred.await()
        }

        if (dogsFallback.isFallback || usersFallback.isFallback) {
            exchange.response.headers.add("X-Fallback", "true")
        }

        val dogMap = dogsFallback.value.associateBy { it.id }
        val userMap = usersFallback.value.associateBy { it.id }

        return NearbyWalksResponse(filteredWalks.mapNotNull { walk ->
            val dog = dogMap[walk.dogId] ?: return@mapNotNull null
            val user = userMap[walk.userId] ?: return@mapNotNull null
            NearbyWalkCardResponse(
                walkId = walk.walkId,
                dog = DogCardResponse(
                    id = dog.id,
                    name = dog.name,
                    breed = dog.breed,
                    size = dog.size.name.removePrefix("DOG_SIZE_"),
                    temperaments = dog.temperamentsList,
                    sociability = dog.sociability,
                    photoUrl = dog.photoUrl,
                    vaccinationRegistered = dog.vaccinationRegistered,
                ),
                owner = OwnerCardResponse(
                    id = user.id,
                    nickname = user.nickname,
                    neighborhood = user.neighborhood,
                    profilePhotoUrl = user.profilePhotoUrl,
                ),
                gridDistance = walk.gridDistance,
                startedAt = walk.startedAt,
            )
        })
    }

    @GetMapping("/me/active")
    suspend fun getMyActiveWalks(@AuthUser userId: Long, exchange: ServerWebExchange): List<WalkResponse> {
        val (walks, isFallback) = cb.executeWithFallback("walks-service", emptyList()) {
            walksClient.getMyActiveWalks(userId)
        }
        // CB OPEN 시 클라이언트에게 fallback 응답임을 알린다
        if (isFallback) exchange.response.headers.add("X-Fallback", "true")
        return walks.map { it.toResponse() }
    }

    private fun WalkInfo.toResponse() = WalkResponse(
        id = id,
        dogId = dogId,
        userId = userId,
        type = type.name.removePrefix("WALK_TYPE_"),
        gridCell = gridCell,
        status = status.name.removePrefix("WALK_STATUS_"),
        startedAt = startedAt,
        endsAt = endsAt,
    )
}
