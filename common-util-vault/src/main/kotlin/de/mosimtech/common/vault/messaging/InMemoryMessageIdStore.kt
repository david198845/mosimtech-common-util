package de.mosimtech.common.vault.messaging

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Gesehene Nachrichten-IDs im Speicher der Instanz, bis ihre Frist abläuft. Abgelaufene Einträge
 * werden bei jedem neuen Eintrag entfernt.
 *
 * Reicht nur für Queues, die genau eine Instanz liest – etwa eine exklusive, flüchtige Queue je
 * Instanz hinter einem Fanout-Exchange. Lesen mehrere Instanzen dieselbe Queue oder muss der Schutz
 * einen Neustart überdauern, braucht es einen gemeinsamen Speicher (z. B. Redis).
 */
class InMemoryMessageIdStore(private val clock: Clock = Clock.systemUTC()) : MessageIdStore {

    private val seen = ConcurrentHashMap<String, Instant>()

    override fun storeIfAbsent(messageId: String, ttl: Duration): Boolean {
        val now = clock.instant()
        seen.entries.removeIf { !it.value.isAfter(now) }
        return seen.putIfAbsent(messageId, now.plus(ttl)) == null
    }

    /** Anzahl der vorgehaltenen IDs; nur für Tests. */
    internal fun size(): Int = seen.size
}
