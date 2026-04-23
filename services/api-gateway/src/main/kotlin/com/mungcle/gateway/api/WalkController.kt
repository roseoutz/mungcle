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

@RestController
@RequestMapping("/v1/walks")
class WalkController(
    private val walksClient: WalksClient,
    private val identityClient: IdentityClient,
    private val petProfileClient: PetProfileClient,
) {

    @PostMapping("/start")
    suspend fun startWalk(@AuthUser userId: Long, @Valid @RequestBody req: StartWalkRequest): WalkResponse {
        val walkType = if (req.open) WalkType.WALK_TYPE_OPEN else WalkType.WALK_TYPE_SOLO
        return walksClient.startWalk(userId, req.dogId, walkType, req.lat, req.lng).toResponse()
    }

    @PostMapping("/{id}/stop")
    suspend fun stopWalk(@AuthUser userId: Long, @PathVariable id: Long): WalkResponse =
        walksClient.stopWalk(walkId = id, userId = userId).toResponse()

    @GetMapping("/nearby")
    suspend fun getNearbyWalks(
        @AuthUser userId: Long,
        @RequestParam lat: Double,
        @RequestParam lng: Double,
    ): NearbyWalksResponse {
        val gridCell = GridCell.fromCoordinates(lat, lng).value

        val (blockedUserIds, nearbyWalks) = coroutineScope {
            val blocked = async { identityClient.getBlockedUserIds(userId) }
            val walks = async { walksClient.getNearbyWalks(gridCell, userId, emptyList()) }
            blocked.await() to walks.await()
        }

        val filteredWalks = nearbyWalks.filter { it.userId !in blockedUserIds }

        val dogIds = filteredWalks.map { it.dogId }.distinct()
        val userIds = filteredWalks.map { it.userId }.distinct()

        val (dogs, users) = coroutineScope {
            val dogsDeferred = async { petProfileClient.getDogsByIds(dogIds) }
            val usersDeferred = async { identityClient.getUsersByIds(userIds) }
            dogsDeferred.await() to usersDeferred.await()
        }

        val dogMap = dogs.associateBy { it.id }
        val userMap = users.associateBy { it.id }

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
    suspend fun getMyActiveWalks(@AuthUser userId: Long): List<WalkResponse> =
        walksClient.getMyActiveWalks(userId).map { it.toResponse() }

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
