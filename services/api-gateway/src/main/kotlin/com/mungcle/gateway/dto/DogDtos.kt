package com.mungcle.gateway.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateDogRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val breed: String,
    @field:NotNull val size: String,
    val temperaments: List<String> = emptyList(),
    @field:Min(1) @field:Max(5) val sociability: Int,
    val photoPath: String? = null,
    val vaccinationPhotoPath: String? = null,
)

data class UpdateDogRequest(
    val name: String? = null,
    val breed: String? = null,
    val size: String? = null,
    val temperaments: List<String>? = null,
    @field:Min(1) @field:Max(5) val sociability: Int? = null,
    val photoPath: String? = null,
    val vaccinationPhotoPath: String? = null,
)

data class DogResponse(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val breed: String,
    val size: String,
    val temperaments: List<String>,
    val sociability: Int,
    val photoUrl: String,
    val vaccinationRegistered: Boolean,
)
