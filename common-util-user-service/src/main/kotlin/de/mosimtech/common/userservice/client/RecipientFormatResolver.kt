package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.LocalizedFormats
import de.mosimtech.common.core.urn.Urn
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Formatvorgaben eines Empfängers (Sprache, Datum, Uhrzeit, Einheiten, Wochenbeginn, Währung,
 * Zeitzone) für Nachrichten ohne Anfrage des Nutzers und für Werte, die nicht im Token stehen
 * (Währung). Ist der User Service nicht erreichbar, gelten die Vorgaben von [FormatPreferences]
 * (Deutsch, alles `AUTO`, Währung und Zeitzone unbekannt) — der Aufruf wirft nie.
 *
 * Zwischengespeichert wie beim [RecipientLanguageResolver]; eine Änderung im Profil wirkt
 * spätestens nach [cacheTtl]. Abgelaufene Einträge werden beim nächsten Abruf eines anderen
 * Nutzers entfernt, der Cache wächst also nicht über die aktiven Empfänger hinaus.
 */
class RecipientFormatResolver(
    private val client: UserLocaleClient,
    private val tokens: ServiceAccessTokenSupplier,
    private val cacheTtl: Duration = Duration.ofMinutes(10),
    private val failureTtl: Duration = Duration.ofMinutes(1),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cache = ConcurrentHashMap<Urn, Pair<FormatPreferences, Instant>>()

    fun resolve(userId: Urn): FormatPreferences {
        val now = clock.instant()
        cache[userId]?.takeIf { (_, expiresAt) -> expiresAt.isAfter(now) }?.let { return it.first }

        val (preferences, ttl) = try {
            (client.findLocale(userId, tokens.accessToken()).block()?.toFormatPreferences() ?: DEFAULT) to cacheTtl
        } catch (error: Exception) {
            logger.warn("Could not read recipient format preferences, using defaults: {}", error.javaClass.simpleName)
            DEFAULT to failureTtl
        }
        cache.entries.removeIf { (_, entry) -> !entry.second.isAfter(now) }
        cache[userId] = preferences to now.plus(ttl)
        return preferences
    }

    fun formats(userId: Urn): LocalizedFormats = LocalizedFormats(resolve(userId))

    /** Anzahl der Einträge im Cache; abgelaufene werden bei jedem neuen Eintrag entfernt. */
    internal fun cachedEntryCount(): Int = cache.size

    /** Verwirft den gespeicherten Wert, z. B. nach einer gemeldeten Profiländerung. */
    fun evict(userId: Urn) {
        cache.remove(userId)
    }

    companion object {
        private val DEFAULT = FormatPreferences()

        /**
         * Ein Resolver, der den User Service nie fragt und immer die Vorgaben von
         * [FormatPreferences] liefert (Deutsch, alles `AUTO`, Währung und Zeitzone unbekannt).
         * Ihn richtet [RecipientDefaultsAutoConfiguration] ein, wenn der Dienst kein Dienstkonto-Token
         * bereitstellt – Aufrufer brauchen dann keinen eigenen Ersatzweg.
         */
        fun defaultsOnly(): RecipientFormatResolver =
            RecipientFormatResolver(UserLocaleClient.offline(), { "" })
    }
}
