package de.modulix.mosimtech.converter.keycloak

import de.modulix.mosimtech.builder.UrnBuilder
import de.modulix.mosimtech.database.namespace.UserNamespace
import de.modulix.mosimtech.database.urn.Urn

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
        if(keycloakUserId.isEmpty() || realm.isEmpty()) throw IllegalArgumentException("keycloakUserId and realm must not be empty")
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