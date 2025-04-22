package de.modulix.mosimtech.security

import de.modulix.mosimtech.converter.keycloak.KeycloakUserUrnConverter
import de.modulix.mosimtech.database.urn.Urn
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SecurityContextAdapter {
    val ADMIN_ROLE_LIST = mutableListOf(ROLE_ADMIN)
    lateinit var REALM: String

    private val authentication: Authentication?
        get() = SecurityContextHolder.getContext().authentication

    fun getCurrentUserID(): Urn? = (authentication as? JwtAuthenticationToken)?.token?.subject
        ?.let { KeycloakUserUrnConverter.convertToUrn(it, REALM) }

    fun getCurrentToken() = (authentication as? JwtAuthenticationToken)?.token

    fun getCurrentUserIDAsString(): String? = getCurrentUserID()?.toUrnString()

    fun hasRole(role: String): Boolean =
        authentication?.authorities?.any { it.authority == role } ?: false

    fun hasAnyRole(roles: List<String>): Boolean = roles.any { hasRole(it) }

    fun hasRoleOrIsAdmin(role: String): Boolean = hasRole(role) || isAdmin()

    fun hasAnyRoleOrIsAdmin(roles: List<String>): Boolean = hasAnyRole(roles) || isAdmin()

    fun getRoles(): List<String> = authentication?.authorities?.map { it.authority } ?: emptyList()

    fun isAdmin(): Boolean = hasAnyRole(ADMIN_ROLE_LIST)
}
