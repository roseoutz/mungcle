package com.mungcle.gateway.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateReportRequest(
    @field:NotNull val reportedUserId: Long,
    @field:NotBlank val reason: String,
)
