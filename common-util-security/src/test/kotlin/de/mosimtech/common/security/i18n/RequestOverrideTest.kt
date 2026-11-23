package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.UnitSystem
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.ZoneId
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RequestOverrideTest {

    @AfterTest
    fun cleanUp() = SecurityContextHolder.clearContext()

    @Test
    fun `language override wins over the token and is restored afterwards`() {
        authenticate("locale" to "ru")

        val inside = RequestLanguage.using(AccountLanguage.ENGLISH) {
            val nested = RequestLanguage.using(AccountLanguage.SPANISH) { RequestLanguage.current() }
            nested to RequestLanguage.current()
        }

        assertEquals(AccountLanguage.SPANISH to AccountLanguage.ENGLISH, inside)
        assertEquals(AccountLanguage.RUSSIAN, RequestLanguage.current())
    }

    @Test
    fun `language override is restored after an exception and stays on its thread`() {
        assertFailsWith<IllegalStateException> {
            RequestLanguage.using(AccountLanguage.ENGLISH) { error("boom") }
        }
        assertEquals(AccountLanguage.DEFAULT, RequestLanguage.current())

        val executor = Executors.newSingleThreadExecutor()
        try {
            val other = RequestLanguage.using(AccountLanguage.ENGLISH) {
                executor.submit<AccountLanguage> { RequestLanguage.current() }.get()
            }
            assertEquals(AccountLanguage.DEFAULT, other)
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `format override replaces the claims and is still completed from the profile`() {
        authenticate("locale" to "de", "unit_system" to "METRIC")
        val fixed = FormatPreferences(language = AccountLanguage.ENGLISH, unitSystem = UnitSystem.IMPERIAL)
        val profile = FormatPreferences(currency = "USD", timezone = ZoneId.of("America/New_York"))

        val inside = RequestFormats.using(fixed) { RequestFormats.current(profile) }

        assertEquals(fixed.copy(currency = "USD", timezone = ZoneId.of("America/New_York")), inside)
        assertEquals(UnitSystem.METRIC, RequestFormats.current().unitSystem)
    }

    @Test
    fun `format override is restored after nesting and exceptions`() {
        val outer = FormatPreferences(unitSystem = UnitSystem.IMPERIAL)
        val inner = FormatPreferences(unitSystem = UnitSystem.METRIC)

        RequestFormats.using(outer) {
            assertEquals(UnitSystem.METRIC, RequestFormats.using(inner) { RequestFormats.current().unitSystem })
            assertFailsWith<IllegalStateException> { RequestFormats.using(inner) { error("boom") } }
            assertEquals(UnitSystem.IMPERIAL, RequestFormats.current().unitSystem)
        }
        assertEquals(UnitSystem.AUTO, RequestFormats.current().unitSystem)
    }

    private fun authenticate(vararg claims: Pair<String, String>) {
        val builder = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user")
        claims.forEach { (name, value) -> builder.claim(name, value) }
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(builder.build())
    }
}
