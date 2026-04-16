package de.mosimtech.common.security

object DelegationConstants {
    /** Keycloak-Rolle für System-zu-System-Aufrufe via Service-Account */
    const val SYSTEM_INVOKE = "system:invoke"

    const val SYSTEM_ASYNC_PUBLISH = "system:async-publish"

    const val SYSTEM_TOKEN_EXCHANGE = "system:token-exchange"

    /** HTTP-Header, der die Target-User-ID für delegierte Anfragen trägt. */
    const val HEADER_TARGET_USER_ID = "X-Target-User-Id"

    /** UMA-Ressourcentyp für Schicht-Daten (wird in Keycloak als Resource type gesetzt). */
    const val UMA_RESOURCE_TYPE_SHIFT = "urn:momasoft:shift"
}
