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
        val blockedResult = cb.executeWithFallback("identity-service", emptyList<Long>()) {
            identityClient.getBlockedUserIds(userId)
        }
        val patternsResult = cb.executeWithFallback("walks-service", emptyList()) {
            walksClient.getNearbyPatterns(gridCell, userId, blockedResult.value)
        }
        // 어느 한 서비스라도 fallback이면 X-Fallback 헤더 설정
        if (blockedResult.isFallback || patternsResult.isFallback) {
            exchange.response.headers.add("X-Fallback", "true")
        }
        return NearbyPatternsResponse(patternsResult.value.map { pattern ->
            PatternResponse(
                dogId = pattern.dogId,
                typicalHour = pattern.typicalHour,
                countLast14Days = pattern.countLast14Days,
            )
        })
    }
}
