package de.mosimtech.common.delegation

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class DelegationHeaderFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val targetUserId = request.getHeader("X-Target-User-Id")
        if (!targetUserId.isNullOrBlank()) {
            DelegationContextHolder.set(targetUserId)
        }
        try {
            filterChain.doFilter(request, response)
        } finally {
            DelegationContextHolder.clear()
        }
    }
}
