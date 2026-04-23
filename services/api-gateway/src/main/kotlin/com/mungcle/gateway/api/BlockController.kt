package com.mungcle.gateway.api

import com.mungcle.gateway.dto.BlockResponse
import com.mungcle.gateway.dto.CreateBlockRequest
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/blocks")
class BlockController(
    private val identityClient: IdentityClient,
    private val cb: CircuitBreakerWrapper,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBlock(@AuthUser userId: Long, @Valid @RequestBody req: CreateBlockRequest) {
        cb.execute("identity-service") { identityClient.createBlock(blockerId = userId, blockedId = req.blockedUserId) }
    }

    @DeleteMapping("/{blockedUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteBlock(@AuthUser userId: Long, @PathVariable blockedUserId: Long) {
        cb.execute("identity-service") { identityClient.deleteBlock(blockerId = userId, blockedId = blockedUserId) }
    }

    @GetMapping
    suspend fun listBlocks(@AuthUser userId: Long): List<BlockResponse> =
        cb.execute("identity-service") { identityClient.listBlocks(userId) }.blocksList.map { block ->
            BlockResponse(
                blockedUserId = block.blockedUserId,
                blockedNickname = block.blockedNickname,
                createdAt = block.createdAt,
            )
        }
}
