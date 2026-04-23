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
import org.springframework.web.server.ServerWebExchange

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
    suspend fun listBlocks(@AuthUser userId: Long, exchange: ServerWebExchange): List<BlockResponse> {
        // listBlocks 응답은 proto 래퍼이므로 매핑까지 block 안에서 수행
        val emptyFallback = emptyList<BlockResponse>()
        val (blocks, isFallback) = cb.executeWithFallback("identity-service", emptyFallback) {
            identityClient.listBlocks(userId).blocksList.map { block ->
                BlockResponse(
                    blockedUserId = block.blockedUserId,
                    blockedNickname = block.blockedNickname,
                    createdAt = block.createdAt,
                )
            }
        }
        // CB OPEN 시 클라이언트에게 fallback 응답임을 알린다
        if (isFallback) exchange.response.headers.add("X-Fallback", "true")
        return blocks
    }
}
