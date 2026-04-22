package com.mungcle.gateway.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateGreetingRequest(
    @field:NotNull val senderDogId: Long,
    @field:NotNull val receiverWalkId: Long,
)

data class RespondGreetingRequest(
    @field:NotNull val accept: Boolean,
)

data class SendMessageRequest(
    @field:NotBlank @field:Size(max = 140) val body: String,
)

data class GreetingResponse(
    val id: Long,
    val senderUserId: Long,
    val receiverUserId: Long,
    val senderDogId: Long,
    val receiverDogId: Long,
    val receiverWalkId: Long,
    val status: String,
    val createdAt: Long,
    val respondedAt: Long?,
    val expiresAt: Long?,
)

data class MessageResponse(
    val id: Long,
    val greetingId: Long,
    val senderUserId: Long,
    val body: String,
    val createdAt: Long,
)
