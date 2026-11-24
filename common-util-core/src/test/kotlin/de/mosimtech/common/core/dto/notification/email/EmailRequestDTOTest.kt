package de.mosimtech.common.core.dto.notification.email

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

internal class EmailRequestDTOTest {

    private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    @Test
    fun `survives a json round trip and ignores unknown properties`() {
        val request = EmailRequestDTO(
            to = "anna@example.com",
            templateId = "delegation-invite",
            locale = "en",
            variables = mapOf("grantorName" to "Anna Chef", "validDays" to "7"),
        )

        val json = objectMapper.writeValueAsString(request)
        assertEquals(request, objectMapper.readValue(json, EmailRequestDTO::class.java))

        val withUnknown = """{"to":"anna@example.com","templateId":"delegation-ended","unknown":"x"}"""
        val parsed = objectMapper.readValue(withUnknown, EmailRequestDTO::class.java)
        assertEquals(EmailRequestDTO(to = "anna@example.com", templateId = "delegation-ended"), parsed)
        assertEquals("de", parsed.locale)
    }
}
