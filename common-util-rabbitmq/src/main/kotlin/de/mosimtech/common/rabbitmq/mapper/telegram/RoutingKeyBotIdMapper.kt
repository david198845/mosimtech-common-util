package de.mosimtech.common.rabbitmq.mapper.telegram

/**
 * A utility class for mapping between routing keys and bot IDs, providing a way to
 * extract bot IDs from routing keys and construct routing keys from bot IDs.
 */
class RoutingKeyBotIdMapper {

    private val pattern = Regex("""^telegram\.(notification|command)\.(.+?)\.routingkey$""")

    /**
     * Extracts the bot ID from the given routing key if it matches the expected pattern.
     *
     * @param routingKey the routing key string to extract the bot ID from
     * @return the extracted bot ID if the routing key matches the pattern, or null if it does not match
     */
    fun mapToBotId(routingKey: String): String? =
        pattern.matchEntire(routingKey)?.groupValues?.get(2)


    /**
     * Constructs a routing key string using the provided bot ID.
     *
     * @param botId the bot ID to include in the routing key
     * @return the constructed routing key string in the format "telegram.notification.<botId>.routingkey"
     */
    fun mapToRoutingKey(botId: String, routingKeyType: RoutingKeyType = RoutingKeyType.NOTIFICATION): String =
        "telegram.${routingKeyType.value}.$botId.routingkey"
}
