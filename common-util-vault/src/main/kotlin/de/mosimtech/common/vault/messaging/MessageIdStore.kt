package de.mosimtech.common.vault.messaging

import java.time.Duration

interface MessageIdStore {
    /**
     * Speichert die [messageId] mit dem angegebenen [ttl], falls sie noch nicht vorhanden ist.
     * @return `true` wenn neu gespeichert (neue Nachricht), `false` wenn bereits vorhanden (Duplikat)
     */
    fun storeIfAbsent(messageId: String, ttl: Duration): Boolean
}
