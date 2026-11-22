package de.mosimtech.common.security.haapikey

import de.mosimtech.common.core.util.toUrn
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Fragt den zentralen Auth-Dienst, ob ein Home-Assistant-Schlüssel gültig ist.
 *
 * Bestätigte Schlüssel werden nicht gemerkt, damit ein Widerruf sofort wirkt. Unbekannte Schlüssel
 * merkt sich der Prüfer für [unknownTtl] unter dem SHA-256 des Schlüssels, damit geratene Schlüssel
 * den Auth-Dienst nicht fluten. Abgelaufene Einträge werden beim ersten Aufruf einer neuen Minute
 * entfernt. Weder Log noch [HaApiKeyVerification.Unavailable.reason] enthalten Schlüssel oder Hash.
 *
 * @param restClient Client mit der Basis-URL des Auth-Dienstes
 * @param tokenSupplier liefert das M2M-Token, mit dem sich dieser Dienst beim Auth-Dienst ausweist
 */
class HaApiKeyVerifier(
    private val restClient: RestClient,
    private val tokenSupplier: () -> String,
    private val clock: Clock,
    private val unknownTtl: Duration = Duration.ofMinutes(1),
) {

    private val unknownUntil = ConcurrentHashMap<String, Instant>()
    private val lastPurgedMinute = AtomicLong(Long.MIN_VALUE)

    fun verify(rawKey: String): HaApiKeyVerification {
        val now = clock.instant()
        purgeExpired(now)
        val keyHash = HaApiKeyHash.sha256Hex(rawKey)
        val rememberedUntil = unknownUntil[keyHash]
        if (rememberedUntil != null && now.isBefore(rememberedUntil)) {
            return HaApiKeyVerification.Unknown
        }
        return try {
            call(rawKey, keyHash, now)
        } catch (e: Exception) {
            // Nur Typen, keine Nachrichten: Texte von Transportfehlern sollen nicht ungeprüft ins Log.
            // Der Typ der Ursache trennt z. B. Zeitüberschreitung von abgewiesener Verbindung.
            val failure = listOfNotNull(e.javaClass.simpleName, e.cause?.javaClass?.simpleName).joinToString("/")
            log.warn("Verification of Home Assistant API key failed: {}", failure)
            HaApiKeyVerification.Unavailable("verification failed: $failure")
        }
    }

    private fun call(rawKey: String, keyHash: String, now: Instant): HaApiKeyVerification {
        val token = tokenSupplier()
        return restClient.post()
            .uri(VERIFY_PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(HaApiKeyVerifyRequest(rawKey))
            .exchange { _, response ->
                val status = response.statusCode
                when {
                    status.value() == 200 -> accepted(response.bodyTo(HaApiKeyVerifyResponse::class.java))
                    status.value() == 404 -> notFound(response.body.readNBytes(1).isEmpty(), keyHash, now)
                    status.value() == 401 || status.value() == 403 -> {
                        val rejection = listOfNotNull(status.value().toString(), errorCode(response)).joinToString(" ")
                        log.error("Auth service rejected this service as caller of the key verification: {}", rejection)
                        HaApiKeyVerification.Unavailable("auth service rejected caller: $rejection")
                    }
                    else -> {
                        log.warn("Auth service answered the key verification with status {}", status.value())
                        HaApiKeyVerification.Unavailable("auth service answered with status ${status.value()}")
                    }
                }
            }!!
    }

    /**
     * Der Prüfaufruf meldet einen unbekannten Schlüssel mit 404 **ohne** Antwortkörper. Ein 404 mit
     * Körper stammt von anderswo (Fehlerseite, Gateway, falsche Basis-URL) und darf nicht als
     * unbekannter Schlüssel gemerkt werden, sonst wiese der Dienst jeden Schlüssel ab.
     */
    private fun notFound(emptyBody: Boolean, keyHash: String, now: Instant): HaApiKeyVerification {
        if (!emptyBody) {
            log.error("Verify endpoint not found - check app.ha-api-key.auth-service-url")
            return HaApiKeyVerification.Unavailable("verify endpoint not found")
        }
        unknownUntil[keyHash] = now.plus(unknownTtl)
        return HaApiKeyVerification.Unknown
    }

    /**
     * Liest den Fehlercode aus der Antwort, sofern sie JSON mit einem `code` ist. Übernommen wird nur
     * ein Code aus Großbuchstaben, Ziffern und Unterstrichen, damit nichts anderes aus dem Körper ins
     * Log oder in den Grund gelangt.
     */
    private fun errorCode(response: RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse): String? {
        val code = try {
            response.bodyTo(HaApiKeyErrorResponse::class.java)?.code
        } catch (_: Exception) {
            null
        }
        return code?.takeIf { ERROR_CODE.matches(it) }
    }

    private fun accepted(body: HaApiKeyVerifyResponse?): HaApiKeyVerification {
        val userId = try {
            body?.userId?.toUrn()
        } catch (_: Exception) {
            null
        }
        if (userId == null) {
            log.warn("Auth service returned an unreadable user URN")
            return HaApiKeyVerification.Unavailable("auth service returned an unreadable user")
        }
        return HaApiKeyVerification.Accepted(userId)
    }

    private fun purgeExpired(now: Instant) {
        val minute = now.epochSecond / SECONDS_PER_MINUTE
        val previous = lastPurgedMinute.get()
        if (previous < minute && lastPurgedMinute.compareAndSet(previous, minute)) {
            unknownUntil.values.removeIf { !now.isBefore(it) }
        }
    }

    companion object {
        /** Pfad des Prüfaufrufs, relativ zur Basis-URL des [RestClient]. */
        const val VERIFY_PATH = "/v1/ha-api-key/verify"

        private const val SECONDS_PER_MINUTE = 60L
        private val ERROR_CODE = Regex("[A-Z0-9_]{1,64}")
        private val log = LoggerFactory.getLogger(HaApiKeyVerifier::class.java)
    }
}
