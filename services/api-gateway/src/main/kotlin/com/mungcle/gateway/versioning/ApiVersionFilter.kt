package com.mungcle.gateway.versioning

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiVersionFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestedVersion = request.getHeader(VERSION_HEADER)
        val resolved = if (requestedVersion != null) {
            ApiVersion.fromDate(requestedVersion)
        } else {
            ApiVersion.LATEST
        }
        response.setHeader(VERSION_HEADER, resolved.date.toString())
        filterChain.doFilter(request, response)
    }

    companion object {
        const val VERSION_HEADER = "Mungcle-Version"
    }
}
