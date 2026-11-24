package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.DelegationConstants
import de.mosimtech.common.security.SecurityContextAdapter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Liest den Header `X-Target-User-Id`, lässt die Freigabe vom Fachmodul bestätigen und legt bei Erfolg
 * einen [DelegationContext] in den [DelegationAccessHolder].
 *
 * Das Fachmodul registriert den Filter hinter der Authentifizierung in der Security-Filterkette.
 * Ist der Header gesetzt, aber niemand angemeldet, endet der Request mit 403, damit eine Aktion,
 * die für den Eigentümer gedacht war, nicht im Bestand des Aufrufers landet.
 */
class DelegationAccessFilter(
    private val grantResolver: DelegationGrantResolver,
    private val actorProvider: () -> Urn? = { SecurityContextAdapter.getCurrentUserID() },
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(DelegationConstants.HEADER_TARGET_USER_ID)
        if (header.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val owner = Urn.parse(header.trim())
        val actor = actorProvider()
        if (owner == null || actor == null) {
            if (owner == null) {
                log.warn("Delegation rejected: actor={}, target header is not a urn (length={})", actor, header.length)
            } else {
                log.warn("Delegation rejected: actor=null, target={}, reason=no authenticated user", owner)
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        if (owner == actor) {
            filterChain.doFilter(request, response)
            return
        }

        // Auflösen vor dem try: Ein Fehler hier hinterlässt keinen Kontext.
        val grant = grantResolver.resolve(actor, owner)
        if (grant == null || grant.scopes.isEmpty()) {
            log.warn("Delegation rejected: actor={}, target={}, reason=no grant or empty scopes", actor, owner)
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        DelegationAccessHolder.set(DelegationContext(owner, actor, grant.scopes, grant.delegationId))
        try {
            filterChain.doFilter(request, response)
        } finally {
            DelegationAccessHolder.clear()
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(DelegationAccessFilter::class.java)
    }
}
