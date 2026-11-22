package de.mosimtech.common.core.i18n.enums

/** Uhrzeitformat aus dem Profil (Claim `time_format`); [AUTO] leitet es aus der Sprache ab. */
enum class TimeFormatPreference(val pattern: String?) {
    AUTO(null),

    /** 14:30 */
    H24("HH:mm"),

    /** 2:30 PM — die Tageszeit-Kennung folgt der Sprache. */
    H12("h:mm a");

    companion object {
        /** Liest einen Claim- oder REST-Wert; fehlend oder unbekannt bedeutet [AUTO]. */
        fun fromValue(value: String?): TimeFormatPreference =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: AUTO
    }
}
