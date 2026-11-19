package de.mosimtech.common.security.haapikey

import kotlin.test.Test
import kotlin.test.assertFalse

class HaApiKeyVerifyRequestTest {

    @Test
    fun `toString does not contain the key`() {
        val rawKey = "mha_geheimer-testschluessel"

        assertFalse(HaApiKeyVerifyRequest(rawKey).toString().contains(rawKey))
    }
}
