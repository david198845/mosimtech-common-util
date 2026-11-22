package de.mosimtech.common.security.haapikey

import de.mosimtech.common.core.urn.Urn

/** Ergebnis der Prüfung eines Home-Assistant-Schlüssels beim zentralen Auth-Dienst. */
sealed interface HaApiKeyVerification {

    /** Der Schlüssel ist gültig und gehört dem Nutzer [userId]. */
    data class Accepted(val userId: Urn) : HaApiKeyVerification

    /** Der Auth-Dienst kennt den Schlüssel nicht (oder nicht mehr). */
    data object Unknown : HaApiKeyVerification

    /** Die Prüfung war nicht möglich; [reason] enthält weder Schlüssel noch Hash. */
    data class Unavailable(val reason: String) : HaApiKeyVerification
}
