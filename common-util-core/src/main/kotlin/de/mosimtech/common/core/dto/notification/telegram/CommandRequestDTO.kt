package de.mosimtech.common.core.dto.notification.telegram

import de.mosimtech.common.core.urn.Urn


/**
 * Represents a Telegram command forwarded via RabbitMQ.
 * @param botId identifier of the bot that received the command
 * @param chatId chat identifier where the command was sent
 * @param command the command name without '/'
 * @param arguments any arguments provided with the command
 */
data class CommandRequestDTO(
    val botId: String,
    val userId: Urn,
    val command: String,
    val arguments: String
)
