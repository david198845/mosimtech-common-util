package de.mosimtech.common.security.converter

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeycloakJwtGrantedAuthoritiesConverterTest {

    private val converter = KeycloakJwtGrantedAuthoritiesConverter()

    @Test
    fun `should extract realm roles as ROLE_ prefixed authorities`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access"))
            .thenReturn(mapOf("roles" to listOf("finance-admin", "core-user")))
        whenever(jwt.getClaim<Map<String, Any>>("resource_access")).thenReturn(null)

        val authorities = converter.convert(jwt)

        assertTrue(authorities.any { it.authority == "ROLE_finance-admin" })
        assertTrue(authorities.any { it.authority == "ROLE_core-user" })
        assertEquals(2, authorities.size)
    }

    @Test
    fun `should extract client roles as client-colon-role authorities`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf("momasoft-finance-api" to mapOf("roles" to listOf("finance:manage", "finance:read"))))

        val authorities = converter.convert(jwt)

        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:manage" })
        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:read" })
        assertEquals(2, authorities.size)
    }

    @Test
    fun `should combine realm and client roles`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access"))
            .thenReturn(mapOf("roles" to listOf("core-user")))
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf("momasoft-shift-api" to mapOf("roles" to listOf("shift:view"))))

        val authorities = converter.convert(jwt)

        assertEquals(2, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_core-user" })
        assertTrue(authorities.any { it.authority == "momasoft-shift-api:shift:view" })
    }

    @Test
    fun `should return empty list when both claims are null`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access")).thenReturn(null)

        val authorities = converter.convert(jwt)

        assertTrue(authorities.isEmpty())
    }

    @Test
    fun `should handle multiple clients in resource_access`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf(
                "momasoft-finance-api" to mapOf("roles" to listOf("finance:manage")),
                "momasoft-shift-api" to mapOf("roles" to listOf("system:invoke"))
            ))

        val authorities = converter.convert(jwt)

        assertEquals(2, authorities.size)
        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:manage" })
        assertTrue(authorities.any { it.authority == "momasoft-shift-api:system:invoke" })
    }
}
