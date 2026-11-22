package de.mosimtech.common.core.i18n

import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import java.util.Locale

/**
 * Ableitung von `AUTO` (und fehlendem Claim) aus der Sprache. Muss mit `resolve-formats.ts` in
 * ui-core übereinstimmen — dort dieselben Tabellen pflegen.
 *
 * Entscheidend ist das Land: aus `de-AT` das Land `AT`. Fehlt es (`en`), gilt das Land der
 * Sprache aus [DEFAULT_REGION_BY_LANGUAGE]. Englisch ohne Land ist bewusst britisch (Tag vor
 * Monat, 24 Stunden, metrisch, Montag); amerikanisch wird es nur mit `en-US` oder mit ausdrücklich
 * gewählten Formaten. Ein unbekanntes Land bekommt DMY_DOT, H24, METRIC und MONDAY.
 */
object RegionFormatDefaults {

    private val DEFAULT_REGION_BY_LANGUAGE = mapOf(
        "de" to "DE", "en" to "GB", "es" to "ES", "ru" to "RU", "uk" to "UA", "fr" to "FR",
        "it" to "IT", "pt" to "PT", "nl" to "NL", "pl" to "PL", "cs" to "CZ", "sv" to "SE",
        "lt" to "LT", "tr" to "TR", "ja" to "JP", "ko" to "KR", "he" to "IL",
    )

    private val MDY_SLASH_REGIONS = setOf("US")
    private val ISO_REGIONS = setOf("SE", "LT", "CA")
    private val DMY_SLASH_REGIONS = setOf(
        "GB", "IE", "AU", "NZ", "IN", "ES", "FR", "BE", "LU", "IT", "PT", "BR", "GR",
        "MX", "AR", "CL", "CO", "PE", "VE", "UY", "EC",
    )
    private val H12_REGIONS = setOf("US", "CA", "AU", "NZ", "IN", "PH", "PK", "EG")
    private val IMPERIAL_REGIONS = setOf("US", "LR", "MM")
    private val SUNDAY_REGIONS = setOf("US", "CA", "MX", "BR", "JP", "KR", "IL", "PH", "IN")

    private val REGION_PART = Regex("""^([A-Za-z]{2}|[0-9]{3})$""")

    /** Das Land eines Sprach-Tags (`de-AT` → `AT`, `en` → `GB`); `null`, wenn keines bekannt ist. */
    fun regionOf(languageTag: String?): String? {
        val parts = languageTag?.trim()?.takeIf { it.isNotEmpty() }?.split('-', '_') ?: return null
        val explicit = parts.drop(1).firstOrNull { REGION_PART.matches(it) }
        return explicit?.uppercase(Locale.ROOT) ?: DEFAULT_REGION_BY_LANGUAGE[parts.first().lowercase(Locale.ROOT)]
    }

    /** Das Land, das für eine Sprache ohne Länderangabe gilt (`ENGLISH` → `GB`). */
    fun defaultRegion(language: AccountLanguage): String? = DEFAULT_REGION_BY_LANGUAGE[language.code]

    fun dateFormat(region: String?): DateFormatPreference = when (region) {
        in MDY_SLASH_REGIONS -> DateFormatPreference.MDY_SLASH
        in ISO_REGIONS -> DateFormatPreference.ISO
        in DMY_SLASH_REGIONS -> DateFormatPreference.DMY_SLASH
        else -> DateFormatPreference.DMY_DOT
    }

    fun timeFormat(region: String?): TimeFormatPreference =
        if (region in H12_REGIONS) TimeFormatPreference.H12 else TimeFormatPreference.H24

    fun unitSystem(region: String?): UnitSystem =
        if (region in IMPERIAL_REGIONS) UnitSystem.IMPERIAL else UnitSystem.METRIC

    fun firstDayOfWeek(region: String?): FirstDayOfWeekPreference =
        if (region in SUNDAY_REGIONS) FirstDayOfWeekPreference.SUNDAY else FirstDayOfWeekPreference.MONDAY
}
