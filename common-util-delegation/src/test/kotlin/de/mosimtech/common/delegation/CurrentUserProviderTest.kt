package de.mosimtech.common.delegation

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.SecurityContextAdapter
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrentUserProviderTest {

    // urn:user:momasoft:own-uuid
    private val ownUrn: Urn = Urn.parse("urn:user:momasoft:own-uuid")!!

    // urn:user:momasoft:grantor-uuid
    private val grantorUrn: Urn = Urn.parse("urn:user:momasoft:grantor-uuid")!!

    @BeforeTest
    fun setup() {
        SecurityContextAdapter.REALM = "momasoft"
    }

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
        SecurityContextHolder.clearContext()
    }

    private fun setupSecurityContext(uuid: String) {
        val jwt = mock<Jwt>()
        whenever(jwt.subject).thenReturn(uuid)
        val auth = JwtAuthenticationToken(jwt, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    fun `getEffectiveUserId returns own user id when no delegation active`() {
        setupSecurityContext("own-uuid")
        val provider = CurrentUserProvider()

        val result = provider.getEffectiveUserId()

        assertEquals(ownUrn, result)
    }

    @Test
    fun `getEffectiveUserId returns grantor user id when delegation active`() {
        DelegationContextHolder.set(grantorUrn.toUrnString())
        setupSecurityContext("own-uuid")
        val provider = CurrentUserProvider()

        val result = provider.getEffectiveUserId()

        assertEquals(grantorUrn, result)
    }
}
