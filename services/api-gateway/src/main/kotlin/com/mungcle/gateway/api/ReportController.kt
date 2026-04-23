package com.mungcle.gateway.api

import com.mungcle.gateway.dto.CreateReportRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/reports")
class ReportController(
    private val identityClient: IdentityClient,
    private val cb: CircuitBreakerWrapper,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createReport(@AuthUser userId: Long, @Valid @RequestBody req: CreateReportRequest) {
        cb.execute("identity-service") {
            identityClient.createReport(
                reporterId = userId,
                reportedId = req.reportedUserId!!, // validated non-null by @NotNull
                reason = req.reason,
            )
        }
    }
}
