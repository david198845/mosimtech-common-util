package de.mosimtech.common.core.rabbitmq.telegram

import de.mosimtech.common.core.mapper.rabbitmq.telegram.RoutingKeyBotIdMapper
import de.mosimtech.common.core.mapper.rabbitmq.telegram.RoutingKeyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoutingKeyBotIdMapperTest {

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToBotId]
     *
     * This method maps a routing key string in the format "telegram.notification.<botId>.routingkey"
     * to the corresponding botId. If the format of the routing key does not match the expected pattern,
     * the method returns null.
     */
    @Test
    fun `mapToBotId should return botId when routingKey matches expected pattern`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "telegram.notification.bot123.routingkey"

        val result = mapper.mapToBotId(routingKey)

        assertEquals("bot123", result)
    }
    @Test
    fun `mapToBotId should return botId when routingKey matches expected pattern for command`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "telegram.command.bot123.routingkey"

        val result = mapper.mapToBotId(routingKey)

        assertEquals("bot123", result)
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
    fun `mapToBotId should return null when routingKey has extra segments`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "telegram.notification.bot123.routingkey.extra"

        val result = mapper.mapToBotId(routingKey)

        assertNull(result)
    }

    @Test
    fun `mapToBotId should return null when routingKey is missing necessary segments`() {
        val mapper = RoutingKeyBotIdMapper()
        val routingKey = "telegram.notification.routingkey"

        val result = mapper.mapToBotId(routingKey)

        assertNull(result)
    }

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToRoutingKey]
     *
     * This method generates a routing key string in the format "telegram.notification.<botId>.routingkey"
     * for a given botId.
     */
    @Test
    fun `mapToRoutingKey should return correctly formatted routingKey for given botId`() {
        val mapper = RoutingKeyBotIdMapper()
        val botId = "bot123"

        val result = mapper.mapToRoutingKey(botId, RoutingKeyType.NOTIFICATION)

        assertEquals("telegram.notification.bot123.routingkey", result)
    }

}
