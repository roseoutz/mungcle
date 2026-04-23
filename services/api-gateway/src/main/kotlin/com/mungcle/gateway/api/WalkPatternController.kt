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
    ): NearbyPatternsResponse {
        val gridCell = GridCell.fromCoordinates(lat, lng).value
        val blockedUserIds = cb.execute("identity-service") { identityClient.getBlockedUserIds(userId) }
        val patterns = cb.execute("walks-service") { walksClient.getNearbyPatterns(gridCell, userId, blockedUserIds) }
        return NearbyPatternsResponse(patterns.map { pattern ->
            PatternResponse(
                dogId = pattern.dogId,
                typicalHour = pattern.typicalHour,
                countLast14Days = pattern.countLast14Days,
            )
        })
    }
}
