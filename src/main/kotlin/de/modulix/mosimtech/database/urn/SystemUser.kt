package de.modulix.mosimtech.database.urn

import de.modulix.mosimtech.database.namespace.SystemUserNamespace
import de.modulix.mosimtech.database.namespace.UserNamespace

object SystemUser : Urn(UserNamespace, SystemUserNamespace.identifier) {
    private fun readResolve(): Any = SystemUser
}