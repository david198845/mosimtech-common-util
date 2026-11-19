package de.mosimtech.common.security.haapikey

/** Antwort des Auth-Dienstes bei gültigem Schlüssel; [userId] ist die URN des Inhabers. Veränderlich mit Vorgabewert, damit Jackson ohne Kotlin-Modul lesen kann. */
data class HaApiKeyVerifyResponse(var userId: String = "")
