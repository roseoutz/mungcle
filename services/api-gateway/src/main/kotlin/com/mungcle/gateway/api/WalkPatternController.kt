package com.mungcle.gateway.api

import com.mungcle.common.domain.GridCell
import com.mungcle.gateway.dto.NearbyPatternsResponse
import com.mungcle.gateway.dto.PatternResponse
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.WalksClient
import com.mungcle.gateway.infrastructure.resilience.CircuitBreakerWrapper
import com.mungcle.gateway.infrastructure.security.AuthUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/v1/walk-patterns")
class WalkPatternController(
    private val walksClient: WalksClient,
    private val identityClient: IdentityClient,
    private val cb: CircuitBreakerWrapper,
) {

    @GetMapping("/nearby")
    suspend fun getNearbyPatterns(
        @AuthUser userId: Long,
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        exchange: ServerWebExchange,
    ): NearbyPatternsResponse {
        val gridCell = GridCell.fromCoordinates(lat, lng).value
        // CB OPEN 시 빈 목록 반환 — X-Fallback 헤더로 클라이언트에 알림
        val blockedUserIds = cb.executeWithFallback("identity-service", emptyList()) {
            identityClient.getBlockedUserIds(userId)
        }
        val patterns = cb.executeWithFallback(
            name = "walks-service",
            fallback = emptyList(),
            onFallback = { exchange.response.headers.set("X-Fallback", "true") },
        ) { walksClient.getNearbyPatterns(gridCell, userId, blockedUserIds) }
        return NearbyPatternsResponse(patterns.map { pattern ->
            PatternResponse(
                dogId = pattern.dogId,
                typicalHour = pattern.typicalHour,
                countLast14Days = pattern.countLast14Days,
            )
        })
    }
}
