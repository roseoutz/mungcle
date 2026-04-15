package com.mungcle.common.domain

/**
 * TSID Node ID를 서비스 이름 기반으로 자동 할당.
 * 10 bits = 0~1023 범위.
 */
object TsidConfig {

    private val SERVICE_NODE_MAP = mapOf(
        "api-gateway" to 1,
        "identity" to 2,
        "pet-profile" to 3,
        "walks" to 4,
        "social" to 5,
        "notification" to 6,
    )

    /**
     * 서비스 이름으로 Node ID 조회.
     * 매핑 없으면 hashCode 기반 fallback (0~1023).
     */
    fun nodeIdFor(serviceName: String): Int {
        return SERVICE_NODE_MAP[serviceName]
            ?: (serviceName.hashCode() and 0x3FF)
    }
}
