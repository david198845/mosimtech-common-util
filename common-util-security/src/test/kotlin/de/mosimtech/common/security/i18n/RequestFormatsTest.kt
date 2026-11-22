package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RequestFormatsTest {
    private val request = MockHttpServletRequest()
    private val instant = Instant.parse("2026-12-31T14:05:00Z")

    @AfterTest
    fun cleanUp() {
        RequestContextHolder.resetRequestAttributes()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `reads the four format claims from the token`() {
        authenticate(
            "locale" to "de",
            "date_format" to "MDY_SLASH",
            "time_format" to "H12",
            "unit_system" to "IMPERIAL",
            "first_day_of_week" to "SUNDAY",
        )

        val preferences = RequestFormats.current()

        assertEquals(AccountLanguage.GERMAN, preferences.language)
        assertEquals(DateFormatPreference.MDY_SLASH, preferences.dateFormat)
        assertEquals(TimeFormatPreference.H12, preferences.timeFormat)
        assertEquals(UnitSystem.IMPERIAL, preferences.unitSystem)
        assertEquals(FirstDayOfWeekPreference.SUNDAY, preferences.firstDayOfWeek)
    }

    @Test
    fun `AUTO and missing claims are derived from the language of the token`() {
        authenticate("locale" to "en-US", "date_format" to "AUTO")

        val preferences = RequestFormats.current()

        assertEquals(DateFormatPreference.AUTO, preferences.dateFormat)
        assertEquals(DateFormatPreference.MDY_SLASH, preferences.resolvedDateFormat())
        assertEquals(TimeFormatPreference.H12, preferences.resolvedTimeFormat())
        assertEquals(UnitSystem.IMPERIAL, preferences.resolvedUnitSystem())
        assertEquals(DayOfWeek.SUNDAY, preferences.resolvedFirstDayOfWeek())
    }

    @Test
    fun `without a locale claim German applies like in ui-core, Accept-Language is ignored`() {
        request.addHeader("Accept-Language", "en-US")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        authenticate("date_format" to "AUTO")

        val preferences = RequestFormats.current()

        assertEquals(AccountLanguage.GERMAN, preferences.language)
        assertEquals(DateFormatPreference.DMY_DOT, preferences.resolvedDateFormat())
        assertEquals(TimeFormatPreference.H24, preferences.resolvedTimeFormat())
        assertEquals(UnitSystem.METRIC, preferences.resolvedUnitSystem())
        assertEquals(DayOfWeek.MONDAY, preferences.resolvedFirstDayOfWeek())
    }

    @Test
    fun `without any token German applies as well`() {
        request.addHeader("Accept-Language", "en-US")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        assertEquals(DateFormatPreference.DMY_DOT, RequestFormats.current().resolvedDateFormat())
    }

    @Test
    fun `currency and timezone stay unknown without profile`() {
        authenticate("locale" to "de")

        val preferences = RequestFormats.current()

        assertNull(preferences.currency)
        assertNull(preferences.timezone)
        assertEquals("31.12.2026 15:05", RequestFormats.formats().dateTime(instant))
    }

    @Test
    fun `the zoneinfo claim sets the timezone and wins over the profile`() {
        authenticate("locale" to "de", "zoneinfo" to "Europe/London")
        val profile = FormatPreferences(currency = "GBP", timezone = ZoneId.of("America/New_York"))

        val preferences = RequestFormats.current(profile)

        assertEquals(ZoneId.of("Europe/London"), preferences.timezone)
        assertEquals("GBP", preferences.currency)
        assertEquals("31.12.2026 14:05", RequestFormats.formats(profile).dateTime(instant))
    }

    @Test
    fun `an invalid or missing zoneinfo falls back to the profile timezone`() {
        authenticate("locale" to "de", "zoneinfo" to "Mars/Olympus")
        val profile = FormatPreferences(timezone = ZoneId.of("America/New_York"))

        assertEquals(ZoneId.of("America/New_York"), RequestFormats.current(profile).timezone)
        assertEquals("31.12.2026 09:05", RequestFormats.formats(profile).dateTime(instant))
        assertNull(RequestFormats.current().timezone)
    }

    @Test
    fun `unknown claim values are treated as AUTO`() {
        authenticate("locale" to "de", "unit_system" to "NAUTICAL", "time_format" to "")

        val preferences = RequestFormats.current()

        assertEquals(UnitSystem.AUTO, preferences.unitSystem)
        assertEquals(TimeFormatPreference.AUTO, preferences.timeFormat)
    }

    private fun authenticate(vararg claims: Pair<String, String>) {
        val builder = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user")
        claims.forEach { (name, value) -> builder.claim(name, value) }
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(builder.build())
    }
}
