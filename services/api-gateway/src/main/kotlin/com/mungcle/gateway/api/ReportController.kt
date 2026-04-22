package com.mungcle.gateway.api

import com.mungcle.gateway.dto.CreateReportRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.security.AuthUser
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reports")
class ReportController(private val identityClient: IdentityClient) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReport(@AuthUser userId: Long, @Valid @RequestBody req: CreateReportRequest): Unit = runBlocking {
        identityClient.createReport(
            reporterId = userId,
            reportedId = req.reportedUserId,
            reason = req.reason,
        )
    }
}
