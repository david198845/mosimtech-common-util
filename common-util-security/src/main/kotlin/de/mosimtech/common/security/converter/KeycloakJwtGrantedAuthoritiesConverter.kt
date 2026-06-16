package de.mosimtech.common.security.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Converts a JWT into a collection of GrantedAuthority objects by extracting roles
 * from the "realm_access" and "resource_access" claims in the token.
 *
 * The resulting authorities include:
 * 1. Realm roles prefixed with "ROLE_", based on the "realm_access" claim.
 * 2. Client-specific roles in the format "client-id:role", based on the "resource_access" claim.
 *
 * The converter processes roles as follows:
 * - Realm roles are extracted from "realm_access.roles" and prefixed with "ROLE_".
 * - For each client in "resource_access", roles are extracted from "roles" and formatted as "client-id:role".
 *
 * If either or both claims are absent or null, no corresponding roles will be added to the authorities.
 */
class KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Converts a JWT into a collection of GrantedAuthority objects by extracting roles
     * from the "realm_access" and "resource_access" claims in the token.
     *
     * @param jwt The JWT containing claims from which roles are extracted.
     * @return A collection of GrantedAuthority objects representing the roles found in the JWT.
     *         Realm roles are prefixed with "ROLE_", and client-specific roles are formatted
     *         as "client-id:role".
     */
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()

        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        val realmRoles = realmAccess?.get("roles") as? List<*> ?: emptyList<Any>()
        realmRoles.filterIsInstance<String>().forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access")
        resourceAccess?.forEach { (client, value) ->
            val clientRoles = (value as? Map<*, *>)?.get("roles") as? List<*> ?: emptyList<Any>()
            clientRoles.filterIsInstance<String>().forEach { role ->
                authorities.add(SimpleGrantedAuthority("$client:$role"))
            }
        }

        return authorities
    }
}
