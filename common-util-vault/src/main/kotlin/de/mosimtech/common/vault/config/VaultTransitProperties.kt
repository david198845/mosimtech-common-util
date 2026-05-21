package de.mosimtech.common.vault.config

import de.mosimtech.common.core.config.MosimtechConfigurationPrefixes
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = MosimtechConfigurationPrefixes.VAULT_TRANSIT)
data class VaultTransitProperties(
    val keyName: String = "async-message-signing",
)
