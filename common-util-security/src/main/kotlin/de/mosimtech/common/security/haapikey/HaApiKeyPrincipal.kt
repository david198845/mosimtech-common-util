package de.mosimtech.common.security.haapikey

import de.mosimtech.common.core.urn.Urn

/** Inhaber eines Home-Assistant-Schlüssels; gilt nur in der Einlieferungskette. */
data class HaApiKeyPrincipal(val userId: Urn)
