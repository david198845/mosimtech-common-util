package de.mosimtech.common.security.config

data class JwtProperties(
    val audience: String = "",
    val allowedAzpClients: List<String> = emptyList(),
)
