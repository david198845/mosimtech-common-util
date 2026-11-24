package de.mosimtech.common.delegation.access

/**
 * Gültige Freigabe, wie das Fachmodul sie auflöst.
 *
 * @property delegationId Kennung der Freigabe, auf der der Zugriff beruht.
 * @property scopes Eingeräumte Rechte. Eine leere Menge gilt als „keine Freigabe“.
 */
data class DelegationGrant(
    val delegationId: String,
    val scopes: Set<String>,
)
