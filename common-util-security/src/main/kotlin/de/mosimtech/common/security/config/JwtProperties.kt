package de.mosimtech.common.security.config

import de.mosimtech.common.core.config.MosimtechConfigurationPrefixes
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = MosimtechConfigurationPrefixes.JWT)
data class JwtProperties(
    val audience: String = "",
    val allowedAzpClients: List<String> = emptyList(),
)
