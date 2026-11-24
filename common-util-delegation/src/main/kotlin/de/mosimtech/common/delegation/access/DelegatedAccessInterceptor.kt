package de.mosimtech.common.delegation.access

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Standard „verboten" für delegierte Requests: Ist ein [DelegationContext] gesetzt, kommt nur eine
 * Controller-Methode durch, die mit [DelegatedAccess] markiert ist und deren Recht der Kontext trägt.
 * Ohne Kontext lässt der Interceptor alles passieren.
 *
 * Das Fachmodul registriert ihn für alle Pfade.
 */
class DelegatedAccessInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val context = DelegationAccessHolder.get() ?: return true

        val method = handler as? HandlerMethod
        val required = method?.getMethodAnnotation(DelegatedAccess::class.java)?.scope

        if (required == null || !context.hasScope(required)) {
            log.warn(
                "Delegated request rejected: handler={}, requiredScope={}",
                method?.method?.name ?: handler.javaClass.simpleName,
                required,
            )
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return false
        }
        return true
    }

    private companion object {
        private val log = LoggerFactory.getLogger(DelegatedAccessInterceptor::class.java)
    }
}
