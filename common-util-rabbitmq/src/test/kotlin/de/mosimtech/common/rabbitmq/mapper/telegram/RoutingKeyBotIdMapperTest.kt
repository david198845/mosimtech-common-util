package de.mosimtech.common.rabbitmq.mapper.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoutingKeyBotIdMapperTest {

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToBotId]
     *
     * This method maps a routing key string in the format "notification.telegram.<botId>"
     * to the corresponding botId. If the format of the routing key does not match the expected pattern,
     * the method returns null.
     */
    @Test
    fun `mapToBotId should return botId when routingKey matches expected pattern for notification`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "notification.telegram.bot123"

        val result = mapper.mapToBotId(routingKey)

        assertEquals("bot123", result)
    }

    @Test
    fun `mapToBotId should return botId when routingKey matches expected pattern for command`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "command.telegram.bot123"

        val result = mapper.mapToBotId(routingKey)

        assertEquals("bot123", result)
    }

    @Test
    fun `mapToBotId should return botId for real routing key like stripchat`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "notification.telegram.stripchat"

        val result = mapper.mapToBotId(routingKey)

        assertEquals("stripchat", result)
    }

    @Test
    fun `mapToBotId should return null when routingKey does not match expected pattern`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "invalid.routing.key.format"

        val result = mapper.mapToBotId(routingKey)

        assertNull(result)
    }

    @Test
    fun `mapToBotId should return null when routingKey is an empty string`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = ""

        val result = mapper.mapToBotId(routingKey)

        assertNull(result)
    }

    @Test
    fun `mapToBotId should return null when type segment is missing`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "telegram.bot123"

        val result = mapper.mapToBotId(routingKey)

        assertNull(result)
    }

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToRoutingKey]
     *
     * This method generates a routing key string in the format "notification.telegram.<botId>"
     * for a given botId.
     */
    @Test
    fun `mapToRoutingKey should return correctly formatted routingKey for NOTIFICATION type`() {
        val mapper = RoutingKeyBotIdMapper()
        val botId = "bot123"

        val result = mapper.mapToRoutingKey(botId, RoutingKeyType.NOTIFICATION)

        assertEquals("notification.telegram.bot123", result)
    }

    @Test
    fun `mapToRoutingKey should return correctly formatted routingKey for COMMAND type`() {
        val mapper = RoutingKeyBotIdMapper()
        val botId = "bot123"

        val result = mapper.mapToRoutingKey(botId, RoutingKeyType.COMMAND)

        assertEquals("command.telegram.bot123", result)
    }
}
