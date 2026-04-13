package de.mosimtech.common.security.validator

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertTrue

class AzpValidatorTest {

    @Test
    fun `validate returns success when azp is in whitelist`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui", "momasoft-admin-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn("momasoft-shiftcalc-ui")
        assertTrue(validator.validate(jwt).hasErrors().not())
    }

    @Test
    fun `validate returns failure when azp is not in whitelist`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn("unknown-client")
        assertTrue(validator.validate(jwt).hasErrors())
    }

    @Test
    fun `validate returns failure when azp is null`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn(null)
        assertTrue(validator.validate(jwt).hasErrors())
    }
}
