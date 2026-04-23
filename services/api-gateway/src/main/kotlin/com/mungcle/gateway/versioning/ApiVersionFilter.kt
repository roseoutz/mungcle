package com.mungcle.gateway.versioning

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiVersionFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestedVersion = exchange.request.headers.getFirst(VERSION_HEADER)
        val resolved = if (requestedVersion != null) {
            ApiVersion.fromDate(requestedVersion)
        } else {
            ApiVersion.LATEST
        }
        exchange.attributes[REQUEST_ATTR_KEY] = resolved
        exchange.response.headers.set(VERSION_HEADER, resolved.date.toString())
        return chain.filter(exchange)
    }

    companion object {
        const val VERSION_HEADER = "Mungcle-Version"
        const val REQUEST_ATTR_KEY = "mungcle.api.version"
    }
}
