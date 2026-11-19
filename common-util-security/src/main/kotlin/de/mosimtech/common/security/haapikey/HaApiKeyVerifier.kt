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
            // Nur der Typ: Nachrichten von Transportfehlern sollen nicht ungeprüft ins Log
            log.warn("Verification of Home Assistant API key failed: {}", e.javaClass.simpleName)
            HaApiKeyVerification.Unavailable("verification failed: ${e.javaClass.simpleName}")
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
                    status.value() == 404 -> {
                        unknownUntil[keyHash] = now.plus(unknownTtl)
                        HaApiKeyVerification.Unknown
                    }
                    status.value() == 401 || status.value() == 403 -> {
                        log.error("Auth service rejected this service as caller of the key verification: {}", status.value())
                        HaApiKeyVerification.Unavailable("auth service rejected caller: ${status.value()}")
                    }
                    else -> {
                        log.warn("Auth service answered the key verification with status {}", status.value())
                        HaApiKeyVerification.Unavailable("auth service answered with status ${status.value()}")
                    }
                }
            }!!
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
        private val log = LoggerFactory.getLogger(HaApiKeyVerifier::class.java)
    }
}
