package de.mosimtech.common.security

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.RegionFormatDefaults
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.ZoneId

/**
 * Formatvorgaben aus den Claims eines Tokens, die der User Service als Keycloak-Attribute setzt.
 * Ein fehlender oder unbekannter Wert bedeutet `AUTO`. Die Währung steht bewusst nicht im Token
 * und bleibt unbekannt; die Zeitzone kommt aus [ZONEINFO], sobald Keycloak sie ausliefert.
 */
object FormatPreferenceClaims {

    const val LOCALE = "locale"
    const val DATE_FORMAT = "date_format"
    const val TIME_FORMAT = "time_format"
    const val UNIT_SYSTEM = "unit_system"
    const val FIRST_DAY_OF_WEEK = "first_day_of_week"

    /** Zeitzone als IANA-Kennung (OIDC-Standardclaim). */
    const val ZONEINFO = "zoneinfo"

    /**
     * Vorgaben aus dem Token; ohne `locale`-Claim gelten [fallbackLanguage] und deren Land.
     * Ohne Token bleibt alles `AUTO`.
     */
    fun fromToken(token: Jwt?, fallbackLanguage: AccountLanguage = AccountLanguage.DEFAULT): FormatPreferences {
        val claims = token?.claims.orEmpty()
        val localeTag = claims[LOCALE] as? String
        val language = AccountLanguage.fromTag(localeTag) ?: fallbackLanguage
        return FormatPreferences(
            language = language,
            region = if (localeTag.isNullOrBlank()) RegionFormatDefaults.defaultRegion(language)
            else RegionFormatDefaults.regionOf(localeTag),
            dateFormat = DateFormatPreference.fromValue(claims[DATE_FORMAT] as? String),
            timeFormat = TimeFormatPreference.fromValue(claims[TIME_FORMAT] as? String),
            unitSystem = UnitSystem.fromValue(claims[UNIT_SYSTEM] as? String),
            firstDayOfWeek = FirstDayOfWeekPreference.fromValue(claims[FIRST_DAY_OF_WEEK] as? String),
            timezone = zoneOf(claims[ZONEINFO] as? String),
        )
    }

    fun currentToken(): Jwt? = (SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken)?.token

    private fun zoneOf(value: String?): ZoneId? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { ZoneId.of(it) }.getOrNull() }
}
