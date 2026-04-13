package de.mosimtech.common.security

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecurityContextAdapterTest {

    @BeforeTest
    fun setup() {
        SecurityContextAdapter.REALM = "momasoft"
    }

    @AfterTest
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    private fun setupAuthentication(
        subject: String = "550e8400-e29b-41d4-a716-446655440000",
        authorities: List<SimpleGrantedAuthority> = emptyList()
    ) {
        val jwt = mock<Jwt>()
        whenever(jwt.subject).thenReturn(subject)
        val auth = JwtAuthenticationToken(jwt, authorities)
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    fun `getCurrentUserID returns URN when authenticated`() {
        setupAuthentication()
        val urn = SecurityContextAdapter.getCurrentUserID()
        assertNotNull(urn)
        assertTrue(urn.toUrnString().contains("550e8400-e29b-41d4-a716-446655440000"))
    }

    @Test
    fun `getCurrentUserID returns null when not authenticated`() {
        assertNull(SecurityContextAdapter.getCurrentUserID())
    }

    @Test
    fun `hasRole returns true when authority matches`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_finance-admin")))
        assertTrue(SecurityContextAdapter.hasRole("ROLE_finance-admin"))
    }

    @Test
    fun `hasRole returns false when authority does not match`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_core-user")))
        assertFalse(SecurityContextAdapter.hasRole("ROLE_finance-admin"))
    }

    @Test
    fun `hasClientRole returns true when client-colon-role authority present`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-finance-api:finance:manage")))
        assertTrue(SecurityContextAdapter.hasClientRole("momasoft-finance-api", "finance:manage"))
    }

    @Test
    fun `hasClientRole returns false when authority missing`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-finance-api:finance:read")))
        assertFalse(SecurityContextAdapter.hasClientRole("momasoft-finance-api", "finance:manage"))
    }

    @Test
    fun `getClientRoles returns all roles for a given client`() {
        setupAuthentication(authorities = listOf(
            SimpleGrantedAuthority("momasoft-finance-api:finance:manage"),
            SimpleGrantedAuthority("momasoft-finance-api:finance:read"),
            SimpleGrantedAuthority("momasoft-shift-api:shift:view")
        ))
        val roles = SecurityContextAdapter.getClientRoles("momasoft-finance-api")
        assertEquals(2, roles.size)
        assertTrue(roles.contains("finance:manage"))
        assertTrue(roles.contains("finance:read"))
    }

    @Test
    fun `getClientRoles returns empty list when no roles for client`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_core-user")))
        val roles = SecurityContextAdapter.getClientRoles("momasoft-finance-api")
        assertTrue(roles.isEmpty())
    }

    @Test
    fun `isSystemInvocation returns true when system-invoke present for client`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-shift-api:system:invoke")))
        assertTrue(SecurityContextAdapter.isSystemInvocation("momasoft-shift-api"))
    }

    @Test
    fun `isSystemInvocation returns false when system-invoke missing`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-shift-api:shift:view")))
        assertFalse(SecurityContextAdapter.isSystemInvocation("momasoft-shift-api"))
    }

    @Test
    fun `getRoles returns all authority strings`() {
        setupAuthentication(authorities = listOf(
            SimpleGrantedAuthority("ROLE_core-user"),
            SimpleGrantedAuthority("momasoft-finance-api:finance:read")
        ))
        val roles = SecurityContextAdapter.getRoles()
        assertEquals(2, roles.size)
        assertTrue(roles.contains("ROLE_core-user"))
    }
}
