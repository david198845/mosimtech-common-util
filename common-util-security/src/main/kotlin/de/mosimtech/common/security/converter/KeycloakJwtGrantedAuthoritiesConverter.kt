package de.mosimtech.common.security.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {

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
