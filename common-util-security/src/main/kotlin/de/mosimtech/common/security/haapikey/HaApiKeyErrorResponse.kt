package de.mosimtech.common.security.haapikey

/** Fehlerantwort des Auth-Dienstes; gelesen wird nur [code]. Veränderlich mit Vorgabewert, damit Jackson ohne Kotlin-Modul lesen kann. */
data class HaApiKeyErrorResponse(var code: String = "")
