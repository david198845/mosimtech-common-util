package de.mosimtech.common.delegation

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.delegation.access.DelegationAccessHolder
import de.mosimtech.common.security.SecurityContextAdapter
import org.slf4j.LoggerFactory

/**
 * Liefert die Nutzer, die ein Request betrifft. Zwei Fragen werden getrennt beantwortet:
 *
 * - [getEffectiveUserId]: Wem gehören die Daten, auf denen der Request arbeitet (Eigentümer)?
 *   Bei einer geprüften Delegation ist das der Freigebende, sonst der angemeldete Nutzer.
 * - [getActorUserId]: Wer ist tatsächlich angemeldet (Aufrufer)? Audit-Felder tragen immer den Aufrufer,
 *   nie den Eigentümer.
 *
 * Der Kontext gilt nur im Request-Thread. In asynchroner Verarbeitung (`@Async`, Executor, geplante Jobs)
 * liefert [getEffectiveUserId] mit weitergereichtem Security-Kontext den Aufrufer, ohne Security-Kontext wirft
 * es eine `IllegalStateException`; in beiden Fällen nie den Eigentümer. Wer dort für den Eigentümer arbeiten
 * muss, gibt dessen ID ausdrücklich im Event oder Parameter mit.
 *
 * Diese Klasse ist eine plain class – jeder Service registriert sie als @Bean
 * in seiner eigenen @Configuration-Klasse.
 */
class CurrentUserProvider {

    /**
     * Eigentümer der Daten, auf denen der Request arbeitet.
     *
     * Reihenfolge: geprüfter Kontext, veralteter Holder (nur damit sich Bestehendes nicht ändert),
     * angemeldeter Nutzer.
     */
    @Suppress("DEPRECATION")
    fun getEffectiveUserId(): Urn =
        DelegationAccessHolder.get()?.ownerId
            ?: DelegationContextHolder.get()?.let { legacy ->
                log.warn("Effective user taken from the deprecated, unverified DelegationContextHolder")
                Urn.parse(legacy)
            }
            ?: SecurityContextAdapter.getCurrentUserID()
            ?: throw IllegalStateException("No authenticated user found in SecurityContext")

    /** Der tatsächlich angemeldete Nutzer, unabhängig von einer Delegation. */
    fun getActorUserId(): Urn =
        SecurityContextAdapter.getCurrentUserID()
            ?: throw IllegalStateException("No authenticated user found in SecurityContext")

    private companion object {
        private val log = LoggerFactory.getLogger(CurrentUserProvider::class.java)
    }
}
