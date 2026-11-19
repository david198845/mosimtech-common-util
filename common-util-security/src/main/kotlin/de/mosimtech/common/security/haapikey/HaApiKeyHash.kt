package de.mosimtech.common.security.haapikey

import java.security.MessageDigest

/** Hash eines Home-Assistant-Schlüssels, unter dem Ratenbegrenzung und Merkliste ihn führen. */
object HaApiKeyHash {

    /** SHA-256 des Schlüssels als 64 Zeichen langer Kleinbuchstaben-Hexwert. */
    fun sha256Hex(rawKey: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(rawKey.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
