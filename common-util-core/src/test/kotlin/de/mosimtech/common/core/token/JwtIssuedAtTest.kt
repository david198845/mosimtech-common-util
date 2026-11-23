package de.mosimtech.common.core.token

import java.time.Instant
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JwtIssuedAtTest {

    @Test
    fun `reads iat from an unpadded base64url payload`() {
        assertEquals(Instant.ofEpochSecond(1_700_000_000), JwtIssuedAt.of(jwt("""{"iat":1700000000,"sub":"ü?"}""")))
    }

    @Test
    fun `rejects a token without iat`() {
        val error = assertFailsWith<SecurityException> { JwtIssuedAt.of(jwt("""{"sub":"x"}""")) }
        assertEquals("Token has no iat claim", error.message)
    }

    @Test
    fun `rejects a non numeric iat`() {
        assertFailsWith<SecurityException> { JwtIssuedAt.of(jwt("""{"iat":"yesterday"}""")) }
    }

    @Test
    fun `rejects something that is not a jwt`() {
        assertFailsWith<SecurityException> { JwtIssuedAt.of("opaque-token") }
        assertFailsWith<SecurityException> { JwtIssuedAt.of("a.%%%.c") }
    }

    companion object {
        fun jwt(claims: String): String {
            val encoder = Base64.getUrlEncoder().withoutPadding()
            val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
            return "$header.${encoder.encodeToString(claims.toByteArray())}.sig"
        }
    }
}
