package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn

/**
 * Geprüfter Delegationskontext eines Requests.
 *
 * @property ownerId Eigentümer der Daten, auf denen der Request arbeitet.
 * @property actorId Der tatsächlich angemeldete Nutzer, der im Namen des Eigentümers handelt.
 * @property scopes Rechte, die der Eigentümer dem Aufrufer eingeräumt hat.
 * @property delegationId Kennung der Freigabe, auf der der Zugriff beruht.
 */
data class DelegationContext(
    val ownerId: Urn,
    val actorId: Urn,
    val scopes: Set<String>,
    val delegationId: String,
) {
    /** Exakter Vergleich, ohne Beachtung von Groß-/Kleinschreibung oder Teilstrings. */
    fun hasScope(scope: String): Boolean = scope in scopes
}
