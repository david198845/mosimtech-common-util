package de.mosimtech.common.security.validator

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertTrue

class AudienceValidatorTest {

    @Test
    fun `validate returns success when required audience present`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("momasoft-shift-api", "account"))
        assertTrue(validator.validate(jwt).hasErrors().not())
    }

    @Test
    fun `validate returns failure when required audience missing`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("other-service"))
        assertTrue(validator.validate(jwt).hasErrors())
    }

    @Test
    fun `validate returns failure when audience is null`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(null)
        assertTrue(validator.validate(jwt).hasErrors())
    }
}
