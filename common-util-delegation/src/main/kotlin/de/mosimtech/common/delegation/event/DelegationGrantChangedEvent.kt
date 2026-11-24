package de.mosimtech.common.delegation.event

import java.time.Instant

/**
 * Zustandstragendes Ereignis zu einer Freigabe. Zugleich die Zeile der Abgleich-Antwort des User-Service.
 *
 * Die IDs sind URN-Zeichenketten (`urn:user:<realm>:<uuid>`, `urn:delegation:<uuid>`), damit das Objekt
 * ohne eigenen Serialisierer über RabbitMQ und REST geht.
 *
 * @property grantVersion Ordnungsmerkmal: Eine kleinere oder gleiche Version wird beim Empfänger verworfen.
 * @property active Nur bei angenommener, nicht ruhender Freigabe wahr.
 * @property validUntil Ablaufzeitpunkt der Freigabe; `null`, wenn sie unbefristet ist.
 */
data class DelegationGrantChangedEvent(
    val delegationId: String,
    val module: String,
    val grantorId: String,
    val granteeId: String,
    val grantorDisplayName: String,
    val granteeDisplayName: String,
    val scopes: List<String>,
    val active: Boolean,
    val validUntil: Instant?,
    val grantVersion: Long,
    val changedAt: Instant,
)
