package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.LocalizedFormats
import de.mosimtech.common.core.urn.Urn
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Kontosprache eines Empfängers für Nachrichten, die ohne Anfrage des Nutzers entstehen
 * (geplante Jobs, RabbitMQ-Ereignisse). Ist der User Service nicht erreichbar, gilt
 * [AccountLanguage.DEFAULT] — eine Benachrichtigung scheitert nie an der Sprache.
 *
 * Ergebnisse werden zwischengespeichert: Ein Lauf über viele Posten desselben Nutzers fragt nur
 * einmal, und ein ausgefallener User Service kostet nicht bei jedem Nutzer erneut den Timeout.
 * Ein Sprachwechsel wirkt spätestens nach [cacheTtl]. Abgelaufene Einträge werden beim nächsten
 * Abruf entfernt.
 */
class RecipientLanguageResolver(
    private val client: UserLocaleClient,
    private val tokens: ServiceAccessTokenSupplier,
    private val cacheTtl: Duration = Duration.ofMinutes(10),
    private val failureTtl: Duration = Duration.ofMinutes(1),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cache = ConcurrentHashMap<Urn, CachedLanguage>()

    fun resolve(userId: Urn): AccountLanguage {
        val now = clock.instant()
        cache[userId]?.takeIf { it.expiresAt.isAfter(now) }?.let { return it.language }

        val (language, ttl) = try {
            (client.findLanguage(userId, tokens.accessToken()).block() ?: AccountLanguage.DEFAULT) to cacheTtl
        } catch (error: Exception) {
            logger.warn("Could not read recipient language, using {}: {}", AccountLanguage.DEFAULT, error.javaClass.simpleName)
            AccountLanguage.DEFAULT to failureTtl
        }
        cache.entries.removeIf { (_, entry) -> !entry.expiresAt.isAfter(now) }
        cache[userId] = CachedLanguage(language, now.plus(ttl))
        return language
    }

    fun formats(userId: Urn): LocalizedFormats = LocalizedFormats(resolve(userId))

    /** Anzahl der Einträge im Cache; abgelaufene werden bei jedem neuen Eintrag entfernt. */
    internal fun cachedEntryCount(): Int = cache.size

    /** Verwirft den gespeicherten Wert, z. B. nach einem gemeldeten Sprachwechsel. */
    fun evict(userId: Urn) {
        cache.remove(userId)
    }

    private data class CachedLanguage(val language: AccountLanguage, val expiresAt: Instant)
}
