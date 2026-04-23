package com.mungcle.gateway.versioning

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 요청에서 Mungcle-Version 헤더를 읽어 해석된 버전을 request attribute에 저장하고,
 * 응답 헤더에 해석된 버전을 echo한다.
 *
 * 헤더 없음 / 잘못된 형식 / 미래 날짜 → LATEST 버전으로 fallback.
 */
@Component
@Order(1)
class ApiVersionFilter : OncePerRequestFilter() {

    companion object {
        const val VERSION_HEADER = "Mungcle-Version"
        const val REQUEST_ATTR_KEY = "mungcle.api.version"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val headerValue = request.getHeader(VERSION_HEADER)
        val resolved = if (headerValue != null) {
            ApiVersion.resolve(headerValue) ?: ApiVersion.LATEST
        } else {
            ApiVersion.LATEST
        }

        request.setAttribute(REQUEST_ATTR_KEY, resolved)
        response.setHeader(VERSION_HEADER, resolved.date.toString())

        filterChain.doFilter(request, response)
    }
}
