package de.mosimtech.common.core.dto.vault

import org.springframework.context.ApplicationEvent

/**
 * Event-Klasse für die Benachrichtigung über erneuerte Credentials
 */
class VaultCredentialsRefreshedEvent(
    source: Any
) : ApplicationEvent(source)
