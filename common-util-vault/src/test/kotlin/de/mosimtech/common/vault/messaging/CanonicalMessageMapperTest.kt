package de.mosimtech.common.vault.messaging

import de.mosimtech.common.core.urn.Urn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class CanonicalMessageMapperTest {

    private val mapper = CanonicalMessageMapper.create()

    data class WithOptional(val a: String, val b: String? = null)
    data class WithoutField(val a: String)
    data class Unsorted(val zebra: Int, val apple: Int)
    data class WithMap(val data: Map<String, String>)
    data class WithTime(val t: Instant)
    data class WithUrn(val id: Urn)

    @Test
    fun `null fields are omitted`() {
        assertThat(mapper.writeValueAsString(WithOptional("x"))).isEqualTo("""{"a":"x"}""")
    }

    @Test
    fun `object with null field and object without the field produce identical bytes`() {
        val withNull = mapper.writeValueAsString(WithOptional("x", null))
        val without = mapper.writeValueAsString(WithoutField("x"))
        assertThat(withNull).isEqualTo(without)
    }

    @Test
    fun `properties are sorted alphabetically`() {
        assertThat(mapper.writeValueAsString(Unsorted(zebra = 1, apple = 2)))
            .isEqualTo("""{"apple":2,"zebra":1}""")
    }

    @Test
    fun `map keys are sorted`() {
        val json = mapper.writeValueAsString(WithMap(linkedMapOf("z" to "1", "a" to "2")))
        assertThat(json).isEqualTo("""{"data":{"a":"2","z":"1"}}""")
    }

    @Test
    fun `instant is serialized as ISO-8601 and round-trips`() {
        val t = Instant.parse("2026-06-18T23:10:00.062404389Z")
        val json = mapper.writeValueAsString(WithTime(t))
        assertThat(json).isEqualTo("""{"t":"2026-06-18T23:10:00.062404389Z"}""")
        assertThat(mapper.readValue(json, WithTime::class.java).t).isEqualTo(t)
    }

    @Test
    fun `urn is serialized via toUrnString`() {
        val urn = Urn.parse("urn:user:keycloak:abc")!!
        val json = mapper.writeValueAsString(WithUrn(urn))
        assertThat(json).isEqualTo("""{"id":"urn:user:keycloak:abc"}""")
    }
}
