package de.mosimtech.common.core.i18n.enums

/**
 * Maßeinheiten der Anzeige aus dem Profil (Claim `unit_system`); [AUTO] leitet sie aus der Sprache
 * ab. Gilt nur für die Anzeige — gespeichert wird eine Größe in der Einheit, in der sie eingegeben
 * wurde.
 */
enum class UnitSystem {
    AUTO,

    /** kg, °C */
    METRIC,

    /** lb, °F */
    IMPERIAL;

    companion object {
        /** Liest einen Claim- oder REST-Wert; fehlend oder unbekannt bedeutet [AUTO]. */
        fun fromValue(value: String?): UnitSystem =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: AUTO
    }
}
