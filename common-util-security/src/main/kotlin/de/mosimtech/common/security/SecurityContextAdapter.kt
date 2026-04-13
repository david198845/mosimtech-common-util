package de.mosimtech.common.security

import de.mosimtech.common.core.converter.keycloak.KeycloakUserUrnConverter
import de.mosimtech.common.core.urn.Urn
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SecurityContextAdapter {

    lateinit var REALM: String

    private val authentication: Authentication?
        get() = SecurityContextHolder.getContext().authentication

    fun getCurrentUserID(): Urn? = (authentication as? JwtAuthenticationToken)?.token?.subject
        ?.takeIf { it.isNotBlank() }
        ?.let { KeycloakUserUrnConverter.convertToUrn(it, REALM) }

    fun getCurrentToken() = (authentication as? JwtAuthenticationToken)?.token

    fun getCurrentUserIDAsString(): String? = getCurrentUserID()?.toUrnString()

    fun hasRole(role: String): Boolean =
        authentication?.authorities?.any { it.authority?.equals(role) == true } ?: false

    fun hasAnyRole(roles: List<String>): Boolean = roles.any { hasRole(it) }

    fun getRoles(): List<String> =
        authentication?.authorities?.mapNotNull { it.authority } ?: emptyList()

    fun hasClientRole(client: String, role: String): Boolean =
        hasRole("$client:$role")

    fun getClientRoles(client: String): List<String> =
        authentication?.authorities
            ?.mapNotNull { it.authority }
            ?.filter { it.startsWith("$client:") }
            ?.map { it.removePrefix("$client:") }
            ?: emptyList()

    fun isSystemInvocation(client: String): Boolean =
        hasClientRole(client, SYSTEM_INVOKE)
}
