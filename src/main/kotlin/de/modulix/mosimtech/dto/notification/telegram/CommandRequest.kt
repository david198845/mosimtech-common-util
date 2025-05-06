package de.modulix.mosimtech.dto.notification.telegram

/**
 * Represents a Telegram command forwarded via RabbitMQ.
 * @param botId identifier of the bot that received the command
 * @param chatId chat identifier where the command was sent
 * @param command the command name without '/'
 * @param arguments any arguments provided with the command
 */
data class CommandRequest(
    val botId: String,
    val chatId: String,
    val command: String,
    val arguments: String
)