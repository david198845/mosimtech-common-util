package de.mosimtech.common.delegation.uma

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UmaPermissionEvaluatorTest {

    private val evaluator = UmaPermissionEvaluator()

    private fun buildAuth(permissions: List<Map<String, Any>>): JwtAuthenticationToken {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("authorization"))
            .thenReturn(mapOf("permissions" to permissions))
        return JwtAuthenticationToken(jwt, emptyList())
    }

    @Test
    fun `returns true when scope and grantor match`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_grantor-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertTrue(result)
    }

    @Test
    fun `returns false when scope does not match`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_grantor-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:edit")

        assertFalse(result)
    }

    @Test
    fun `returns false when grantor id not in resource name`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_other-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `returns false when authorization claim missing`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("authorization")).thenReturn(null)
        val auth = JwtAuthenticationToken(jwt, emptyList())

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `returns false when authentication is not JwtAuthenticationToken`() {
        val auth = mock<org.springframework.security.core.Authentication>()

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `hasPermission with domain object always returns false`() {
        val auth = mock<org.springframework.security.core.Authentication>()

        val result = evaluator.hasPermission(auth, Any(), "shift:view")

        assertFalse(result)
    }
}
