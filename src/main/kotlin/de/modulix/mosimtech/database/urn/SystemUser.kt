package de.modulix.mosimtech.database.urn

import de.modulix.mosimtech.database.namespace.UserNamespace

object SystemUser : Urn(UserNamespace, "SYSTEM") {
    private fun readResolve(): Any = SystemUser
}