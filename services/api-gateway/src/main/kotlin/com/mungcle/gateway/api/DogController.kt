package com.mungcle.gateway.api

import com.mungcle.gateway.dto.CreateDogRequest
import com.mungcle.gateway.dto.DogResponse
import com.mungcle.gateway.dto.UpdateDogRequest
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.mungcle.proto.petprofile.v1.DogInfo
import com.mungcle.proto.petprofile.v1.DogSize
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/v1/dogs")
class DogController(
    private val petProfileClient: PetProfileClient,
    private val cb: CircuitBreakerWrapper,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createDog(@AuthUser userId: Long, @Valid @RequestBody req: CreateDogRequest): DogResponse =
        cb.execute("pet-profile-service") {
            petProfileClient.createDog(
                ownerId = userId,
                dogName = req.name,
                dogBreed = req.breed,
                dogSize = parseDogSize(req.size),
                dogTemperaments = req.temperaments,
                dogSociability = req.sociability,
                photoPath = req.photoPath,
                vaccinationPhotoPath = req.vaccinationPhotoPath,
            )
        }.toResponse()

    @GetMapping
    suspend fun getDogs(@AuthUser userId: Long, exchange: ServerWebExchange): List<DogResponse> {
        // CB OPEN 시 빈 배열 반환 — X-Fallback 헤더로 클라이언트에 알림
        val dogs = cb.executeWithFallback(
            name = "pet-profile-service",
            fallback = emptyList(),
            onFallback = { exchange.response.headers.set("X-Fallback", "true") },
        ) { petProfileClient.getDogsByOwner(userId) }
        return dogs.map { it.toResponse() }
    }

    @GetMapping("/{id}")
    suspend fun getDog(@AuthUser userId: Long, @PathVariable id: Long): DogResponse =
        cb.execute("pet-profile-service") { petProfileClient.getDog(id) }.toResponse()

    @PatchMapping("/{id}")
    suspend fun updateDog(
        @AuthUser userId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateDogRequest,
    ): DogResponse =
        cb.execute("pet-profile-service") {
            petProfileClient.updateDog(
                dogId = id,
                requesterId = userId,
                dogName = req.name,
                dogBreed = req.breed,
                dogSize = req.size?.let { parseDogSize(it) },
                dogTemperaments = req.temperaments,
                dogSociability = req.sociability,
                photoPath = req.photoPath,
                vaccinationPhotoPath = req.vaccinationPhotoPath,
            )
        }.toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteDog(@AuthUser userId: Long, @PathVariable id: Long) {
        cb.execute("pet-profile-service") { petProfileClient.deleteDog(dogId = id, ownerId = userId) }
    }

    private fun parseDogSize(size: String): DogSize = when (size.uppercase()) {
        "SMALL" -> DogSize.DOG_SIZE_SMALL
        "MEDIUM" -> DogSize.DOG_SIZE_MEDIUM
        "LARGE" -> DogSize.DOG_SIZE_LARGE
        else -> throw IllegalArgumentException("유효하지 않은 반려견 크기입니다: $size")
    }

    private fun DogInfo.toResponse() = DogResponse(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = breed,
        size = size.name.removePrefix("DOG_SIZE_"),
        temperaments = temperamentsList,
        sociability = sociability,
        photoUrl = photoUrl,
        vaccinationRegistered = vaccinationRegistered,
    )
}
