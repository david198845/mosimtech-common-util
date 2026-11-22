package de.mosimtech.common.userservice.client

/**
 * Liefert das M2M-Token des eigenen Dienstkontos (Client Credentials). Das Dienstkonto braucht
 * am `mosimtech-user-service` die Rolle `system:invoke` und muss dort in der azp-Whitelist stehen.
 *
 * Stellt ein Dienst eine Bean davon bereit, richtet [UserServiceLanguageAutoConfiguration] den
 * [RecipientLanguageResolver] ein.
 */
fun interface ServiceAccessTokenSupplier {
    fun accessToken(): String
}
