package de.mosimtech.common.core.token

import java.time.Instant

/**
 * Repräsentiert ein ausgetauschtes OAuth2-Token mit Ablaufinformation.
 * Wird für Token-Exchange-Ergebnisse (sync 1 min / async 24h) verwendet.
 */
data class ExchangedToken(
    val tokenValue: String,
    val expiresAt: Instant,
) {
    fun isValid(safetyMarginSeconds: Long = 30): Boolean =
        Instant.now().plusSeconds(safetyMarginSeconds).isBefore(expiresAt)
}
