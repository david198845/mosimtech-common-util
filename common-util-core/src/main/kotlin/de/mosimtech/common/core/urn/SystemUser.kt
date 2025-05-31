package de.mosimtech.common.core.urn

import de.mosimtech.common.core.namespace.SystemUserNamespace
import de.mosimtech.common.core.namespace.UserNamespace

object SystemUser : Urn(UserNamespace, SystemUserNamespace.identifier) {
    private fun readResolve(): Any = SystemUser
}
