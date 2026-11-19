package de.mosimtech.common.security.haapikey

import kotlin.test.Test
import kotlin.test.assertEquals

class HaApiKeyHashTest {

    @Test
    fun `sha256 of abc matches the known digest`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            HaApiKeyHash.sha256Hex("abc")
        )
    }

    @Test
    fun `result has 64 characters`() {
        assertEquals(64, HaApiKeyHash.sha256Hex("mha_irgendein-schluessel").length)
    }
}
