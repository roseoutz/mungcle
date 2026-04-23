package com.mungcle.gateway.versioning

import org.springframework.web.server.ServerWebExchange

/**
 * 컨트롤러에서 현재 요청의 해석된 API 버전을 조회하는 헬퍼.
 * 버전 기반 분기가 필요한 경우 사용한다.
 *
 * 사용 예:
 *   val version = ApiVersionHolder.from(exchange)
 *   if (version >= ApiVersion.V_2026_04_23) { ... }
 */
object ApiVersionHolder {

    fun from(exchange: ServerWebExchange): ApiVersion =
        exchange.attributes[ApiVersionFilter.REQUEST_ATTR_KEY] as? ApiVersion
            ?: ApiVersion.LATEST
}
