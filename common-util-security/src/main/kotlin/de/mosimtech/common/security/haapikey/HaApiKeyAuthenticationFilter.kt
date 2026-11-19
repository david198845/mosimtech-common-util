package de.mosimtech.common.security.haapikey

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Authentifiziert Einlieferungen aus Home Assistant über den Header `X-Api-Key`.
 *
 * Fehlender, zu langer und unbekannter Schlüssel werden gleich beantwortet (401, leerer Body), damit
 * die Antwort nicht verrät, ob es einen Schlüssel gibt. Die Ratenbegrenzung greift vor der Prüfung.
 * Ist der Auth-Dienst nicht erreichbar oder lehnt er diesen Dienst ab, antwortet der Filter mit 503:
 * Der Fehler liegt dann nicht beim Schlüssel des Nutzers. Der Schlüssel selbst wird nie protokolliert.
 *
 * Bewusst keine Spring-Bean: Spring Boot würde den Filter sonst zusätzlich in die globale
 * Servlet-Kette hängen, und er liefe vor jeder anderen Anfrage.
 *
 * @param authority Berechtigung, die ein gültiger Schlüssel der Anfrage verleiht
 */
class HaApiKeyAuthenticationFilter(
    private val verifier: HaApiKeyVerifier,
    private val rateLimiter: HaApiKeyRateLimiter,
    authority: String = "ingest:write",
) : OncePerRequestFilter() {

    private val grantedAuthority = SimpleGrantedAuthority(authority)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val rawKey = request.getHeader(HEADER)?.trim()
        if (rawKey.isNullOrEmpty() || rawKey.length > MAX_KEY_LENGTH) {
            reject(response, HttpStatus.UNAUTHORIZED)
            return
        }
        if (!rateLimiter.tryAcquire(HaApiKeyHash.sha256Hex(rawKey))) {
            log.debug("Home Assistant request rate limited")
            reject(response, HttpStatus.TOO_MANY_REQUESTS)
            return
        }
        when (val result = verifier.verify(rawKey)) {
            is HaApiKeyVerification.Unknown -> {
                log.debug("Home Assistant request with unknown API key rejected")
                reject(response, HttpStatus.UNAUTHORIZED)
            }
            is HaApiKeyVerification.Unavailable -> {
                log.debug("Home Assistant API key could not be verified: {}", result.reason)
                reject(response, HttpStatus.SERVICE_UNAVAILABLE)
            }
            is HaApiKeyVerification.Accepted -> {
                SecurityContextHolder.setContext(
                    SecurityContextHolder.createEmptyContext().apply {
                        authentication = UsernamePasswordAuthenticationToken.authenticated(
                            HaApiKeyPrincipal(result.userId), null, listOf(grantedAuthority)
                        )
                    }
                )
                chain.doFilter(request, response)
            }
        }
    }

    // setStatus statt sendError: kein Error-Dispatch, der in eine andere Kette laufen könnte
    private fun reject(response: HttpServletResponse, status: HttpStatus) {
        response.status = status.value()
    }

    companion object {
        const val HEADER = "X-Api-Key"

        /** Ein gültiger Schlüssel hat 47 Zeichen; mehr wird gar nicht erst gehasht. */
        const val MAX_KEY_LENGTH = 128

        private val log = LoggerFactory.getLogger(HaApiKeyAuthenticationFilter::class.java)
    }
}
