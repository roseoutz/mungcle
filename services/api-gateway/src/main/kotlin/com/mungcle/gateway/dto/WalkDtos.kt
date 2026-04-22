package com.mungcle.gateway.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

data class StartWalkRequest(
    @field:NotNull val dogId: Long,
    @field:NotNull @field:DecimalMin("-90.0") @field:DecimalMax("90.0") val lat: Double,
    @field:NotNull @field:DecimalMin("-180.0") @field:DecimalMax("180.0") val lng: Double,
    val open: Boolean = true,
)

data class WalkResponse(
    val id: Long,
    val dogId: Long,
    val userId: Long,
    val type: String,
    val gridCell: String,
    val status: String,
    val startedAt: Long,
    val endsAt: Long,
)

data class DogCardResponse(
    val id: Long,
    val name: String,
    val breed: String,
    val size: String,
    val temperaments: List<String>,
    val sociability: Int,
    val photoUrl: String,
    val vaccinationRegistered: Boolean,
)

data class OwnerCardResponse(
    val id: Long,
    val nickname: String,
    val neighborhood: String,
    val profilePhotoUrl: String,
)

data class NearbyWalkCardResponse(
    val walkId: Long,
    val dog: DogCardResponse,
    val owner: OwnerCardResponse,
    val gridDistance: Int,
    val startedAt: Long,
)

data class NearbyWalksResponse(
    val walks: List<NearbyWalkCardResponse>,
)

data class PatternResponse(
    val dogId: Long,
    val typicalHour: Int,
    val countLast14Days: Int,
)

data class NearbyPatternsResponse(
    val patterns: List<PatternResponse>,
)
