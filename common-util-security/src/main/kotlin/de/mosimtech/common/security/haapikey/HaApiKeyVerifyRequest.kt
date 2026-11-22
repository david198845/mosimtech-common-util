package de.mosimtech.common.security.haapikey

/**
 * Body des Prüfaufrufs an den Auth-Dienst.
 *
 * [toString] gibt den Schlüssel bewusst nicht aus: Der RestClient protokolliert den Body auf
 * DEBUG über genau diese Methode.
 */
data class HaApiKeyVerifyRequest(val key: String) {

    override fun toString(): String = "HaApiKeyVerifyRequest(key=***)"
}
