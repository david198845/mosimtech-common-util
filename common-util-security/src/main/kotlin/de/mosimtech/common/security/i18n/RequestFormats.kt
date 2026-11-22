package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.LocalizedFormats
import de.mosimtech.common.security.FormatPreferenceClaims

/**
 * Formatvorgaben der laufenden Anfrage aus den Token-Claims `locale`, `date_format`,
 * `time_format`, `unit_system`, `first_day_of_week` und `zoneinfo`. Ein fehlender Claim oder
 * `AUTO` wird später aus dem Land der Sprache abgeleitet.
 *
 * Ohne `locale`-Claim gilt `de` — genau wie in ui-core, damit Backend und Oberfläche dieselben
 * Formate zeigen. `Accept-Language` wird hier bewusst **nicht** ausgewertet (anders als bei
 * [RequestLanguage] für Texte).
 *
 * Währung und — solange `zoneinfo` fehlt — Zeitzone sind hier unbekannt. Wer sie braucht, holt
 * das Profil über den User Service (`RecipientFormatResolver`) und gibt es als `profile` mit:
 *
 * ```
 * val formats = RequestFormats.formats(recipientFormats.resolve(userId))
 * ```
 */
object RequestFormats {

    /**
     * Die Vorgaben der Anfrage. Aus [profile] werden nur Werte übernommen, die das Token nicht
     * hat: Währung immer, die Zeitzone nur ohne gültigen `zoneinfo`-Claim.
     */
    fun current(profile: FormatPreferences? = null): FormatPreferences {
        val fromToken = FormatPreferenceClaims.fromToken(FormatPreferenceClaims.currentToken())
        return fromToken.copy(
            currency = fromToken.currency ?: profile?.currency,
            timezone = fromToken.timezone ?: profile?.timezone,
        )
    }

    /** [LocalizedFormats] nach den Vorgaben der Anfrage; unbekannte Zeitzone: Europe/Berlin. */
    fun formats(profile: FormatPreferences? = null): LocalizedFormats = LocalizedFormats(current(profile))
}
