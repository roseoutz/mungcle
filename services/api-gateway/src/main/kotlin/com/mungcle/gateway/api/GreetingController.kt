package com.mungcle.gateway.api

import com.mungcle.gateway.dto.CreateGreetingRequest
import com.mungcle.gateway.dto.GreetingResponse
import com.mungcle.gateway.dto.MessageResponse
import com.mungcle.gateway.dto.RespondGreetingRequest
import com.mungcle.gateway.dto.SendMessageRequest
import com.mungcle.gateway.infrastructure.grpc.SocialClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import com.mungcle.proto.social.v1.GreetingDirection
import com.mungcle.proto.social.v1.GreetingInfo
import com.mungcle.proto.social.v1.GreetingStatus
import com.mungcle.proto.social.v1.MessageInfo
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/greetings")
class GreetingController(
    private val socialClient: SocialClient,
    private val cb: CircuitBreakerWrapper,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createGreeting(
        @AuthUser userId: Long,
        @Valid @RequestBody req: CreateGreetingRequest,
    ): GreetingResponse =
        cb.execute("social-service") {
            socialClient.createGreeting(
                senderUserId = userId,
                senderDogId = req.senderDogId,
                receiverWalkId = req.receiverWalkId,
            )
        }.toResponse()

    @PostMapping("/{id}/respond")
    suspend fun respondGreeting(
        @AuthUser userId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody req: RespondGreetingRequest,
    ): GreetingResponse =
        cb.execute("social-service") {
            socialClient.respondGreeting(
                greetingId = id,
                responderUserId = userId,
                accept = req.accept,
            )
        }.toResponse()

    @GetMapping("/{id}")
    suspend fun getGreeting(@AuthUser userId: Long, @PathVariable id: Long): GreetingResponse =
        cb.execute("social-service") { socialClient.getGreeting(greetingId = id, userId = userId) }.toResponse()

    @GetMapping
    suspend fun listGreetings(
        @AuthUser userId: Long,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) direction: String?,
    ): List<GreetingResponse> {
        val statusFilter = status?.let { parseGreetingStatus(it) }
        val directionFilter = direction?.let { parseGreetingDirection(it) }
        return cb.execute("social-service") { socialClient.listGreetings(userId, statusFilter, directionFilter) }
            .map { it.toResponse() }
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun sendMessage(
        @AuthUser userId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody req: SendMessageRequest,
    ): MessageResponse =
        cb.execute("social-service") {
            socialClient.sendMessage(
                greetingId = id,
                senderUserId = userId,
                body = req.body,
            )
        }.toMessageResponse()

    @GetMapping("/{id}/messages")
    suspend fun listMessages(@AuthUser userId: Long, @PathVariable id: Long): List<MessageResponse> =
        cb.execute("social-service") { socialClient.listMessages(greetingId = id, userId = userId) }
            .map { it.toMessageResponse() }

    private fun parseGreetingStatus(value: String): GreetingStatus = when (value.uppercase()) {
        "PENDING" -> GreetingStatus.GREETING_STATUS_PENDING
        "ACCEPTED" -> GreetingStatus.GREETING_STATUS_ACCEPTED
        "EXPIRED" -> GreetingStatus.GREETING_STATUS_EXPIRED
        else -> throw IllegalArgumentException("유효하지 않은 인사 상태입니다: $value")
    }

    private fun parseGreetingDirection(value: String): GreetingDirection = when (value.uppercase()) {
        "SENT" -> GreetingDirection.GREETING_DIRECTION_SENT
        "RECEIVED" -> GreetingDirection.GREETING_DIRECTION_RECEIVED
        else -> throw IllegalArgumentException("유효하지 않은 인사 방향입니다: $value")
    }

    private fun GreetingInfo.toResponse() = GreetingResponse(
        id = id,
        senderUserId = senderUserId,
        receiverUserId = receiverUserId,
        senderDogId = senderDogId,
        receiverDogId = receiverDogId,
        receiverWalkId = receiverWalkId,
        status = status.name.removePrefix("GREETING_STATUS_"),
        createdAt = createdAt,
        respondedAt = if (hasRespondedAt()) respondedAt else null,
        expiresAt = if (hasExpiresAt()) expiresAt else null,
    )

    private fun MessageInfo.toMessageResponse() = MessageResponse(
        id = id,
        greetingId = greetingId,
        senderUserId = senderUserId,
        body = body,
        createdAt = createdAt,
    )
}
