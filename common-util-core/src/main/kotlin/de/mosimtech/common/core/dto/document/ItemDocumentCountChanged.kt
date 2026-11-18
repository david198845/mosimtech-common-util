package de.mosimtech.common.core.dto.document

import java.time.Instant

/**
 * Ereignis des Dokumentdienstes: Die Zahl der Belege eines Items hat sich geaendert.
 *
 * Es traegt die absolute Anzahl, nicht die Differenz. Damit ist die Verarbeitung
 * idempotent und unempfindlich gegen doppelte oder verspaetete Zustellung.
 *
 * @property externalUrn URN des Items, dem die Belege zugeordnet sind (als String, wie bei `DocumentDTO`)
 * @property userId URN des Besitzers (als String)
 * @property documentCount absolute Zahl der gueltigen Belege des Items
 * @property observedAt Zeitpunkt, zu dem der Dokumentdienst diese Zahl festgestellt hat
 */
data class ItemDocumentCountChanged(
    val externalUrn: String,
    val userId: String,
    val documentCount: Int,
    val observedAt: Instant,
)
