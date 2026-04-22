package com.mungcle.gateway.dto

import jakarta.validation.constraints.NotNull

data class CreateBlockRequest(
    @field:NotNull val blockedUserId: Long,
)

data class BlockResponse(
    val blockedUserId: Long,
    val blockedNickname: String,
    val createdAt: Long,
)
