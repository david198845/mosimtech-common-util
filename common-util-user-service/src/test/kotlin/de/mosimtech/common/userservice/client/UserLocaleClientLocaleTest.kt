package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import de.mosimtech.common.core.urn.Urn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserLocaleClientLocaleTest {
    private val userId = requireNotNull(Urn.of("urn:user:momasoft:person"))

    @Test
    fun `reads every field of the locale endpoint`() {
        val locale = locale(
            """{"data":{"language":"en-GB","timezone":"Europe/London","timeFormat":"H12","dateFormat":"MDY_SLASH",
               "unitSystem":"IMPERIAL","firstDayOfWeek":"SUNDAY","currency":"gbp"}}""",
        )

        assertEquals("en-GB", locale.languageTag)
        assertEquals(AccountLanguage.ENGLISH, locale.language)
        assertEquals(ZoneId.of("Europe/London"), locale.timezone)
        assertEquals(TimeFormatPreference.H12, locale.timeFormat)
        assertEquals(DateFormatPreference.MDY_SLASH, locale.dateFormat)
        assertEquals(UnitSystem.IMPERIAL, locale.unitSystem)
        assertEquals(FirstDayOfWeekPreference.SUNDAY, locale.firstDayOfWeek)
        assertEquals("GBP", locale.currency)
        assertEquals("GB", locale.toFormatPreferences().region)
    }

    @Test
    fun `tolerates an older user service with language and timezone only`() {
        val locale = locale("""{"data":{"language":"de","timezone":"Europe/Berlin"}}""")

        assertEquals(AccountLanguage.GERMAN, locale.language)
        assertEquals(DateFormatPreference.AUTO, locale.dateFormat)
        assertEquals(TimeFormatPreference.AUTO, locale.timeFormat)
        assertEquals(UnitSystem.AUTO, locale.unitSystem)
        assertEquals(FirstDayOfWeekPreference.AUTO, locale.firstDayOfWeek)
        assertNull(locale.currency)
    }

    @Test
    fun `ignores values it cannot use instead of failing`() {
        val locale = locale(
            """{"data":{"language":"fr","timezone":"Mars/Olympus","unitSystem":"NAUTICAL","currency":"XYZW","dateFormat":42}}""",
        )

        assertNull(locale.language)
        assertNull(locale.timezone)
        assertNull(locale.currency)
        assertEquals(UnitSystem.AUTO, locale.unitSystem)
        assertEquals(DateFormatPreference.AUTO, locale.dateFormat)
        assertEquals(AccountLanguage.DEFAULT, locale.toFormatPreferences().language)
    }

    private fun locale(body: String): UserLocaleDTO {
        val client = UserLocaleClient(WebClient.builder()
            .baseUrl("https://user-service/api")
            .exchangeFunction {
                Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .build())
            }
            .build())
        return requireNotNull(client.findLocale(userId, "token").block())
    }
}
