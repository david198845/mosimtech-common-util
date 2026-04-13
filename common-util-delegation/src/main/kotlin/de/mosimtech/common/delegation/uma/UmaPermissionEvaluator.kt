package de.mosimtech.common.delegation.uma

import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.io.Serializable

class UmaPermissionEvaluator : PermissionEvaluator {

    override fun hasPermission(authentication: Authentication, targetDomainObject: Any, permission: Any): Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        val jwt = (authentication as? JwtAuthenticationToken)?.token ?: return false
        val grantorId = targetId.toString()
        val requiredScope = permission.toString()
        val moduleIdentifier = targetType.substringAfterLast(':')

        val authorization = jwt.getClaim<Map<String, Any>>("authorization") ?: return false
        val permissions = authorization["permissions"] as? List<Map<String, Any>> ?: return false

        return permissions.any { perm ->
            val rsName = perm["rsname"] as? String ?: ""
            val scopes = perm["scopes"] as? List<*> ?: emptyList<Any>()
            rsName.contains(grantorId) && rsName.contains(moduleIdentifier) && scopes.contains(requiredScope)
        }
    }
}
