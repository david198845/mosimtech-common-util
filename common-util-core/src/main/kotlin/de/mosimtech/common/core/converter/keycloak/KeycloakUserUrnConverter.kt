package de.mosimtech.common.core.converter.keycloak

import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.core.namespace.UserNamespace
import de.mosimtech.common.core.urn.Urn

/**
 * KeycloakUserUrnConverter is a utility class responsible for converting Keycloak user IDs
 * to URN (Uniform Resource Name) format.
 */
open class KeycloakUserUrnConverter {

    /**
     * Converts a Keycloak user ID and a realm to a URN (Uniform Resource Name).
     *
     * @param keycloakUserId The Keycloak user ID to be converted.
     * @param realm The realm associated with the Keycloak user.
     * @return A URN object representing the Keycloak user ID in the specified realm.
     */
    open fun convertToUrn(keycloakUserId: String, realm: String): Urn {
        require(keycloakUserId.isNotBlank() && realm.isNotBlank()) { "keycloakUserId and realm must not be empty" }
        if(keycloakUserId.startsWith("urn:${UserNamespace.identifier}:")) return Urn.parse(keycloakUserId)!!
        return UrnBuilder()
            .withNamespace(UserNamespace)
            .withSubNamespaceIdentifier(realm)
            .withNamespaceSpecificString(keycloakUserId)
            .build()
    }

    companion object {
        /**
         * Converts a Keycloak user ID and a realm to a URN (Uniform Resource Name).
         *
         * @param keycloakUserId The Keycloak user ID to be converted.
         * @param realm The realm associated with the Keycloak user.
         * @return A URN object representing the Keycloak user ID in the specified realm.
         */
        fun convertToUrn(keycloakUserId: String, realm: String): Urn {
            return KeycloakUserUrnConverter().convertToUrn(keycloakUserId, realm)
        }
    }

}
