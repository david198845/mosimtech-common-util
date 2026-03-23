package de.mosimtech.common.rabbitmq.mapper.telegram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoutingKeyBotIdMapperTest {

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToBotId]
     *
     * Supports two formats:
     * - Notification: "notification.telegram.<botId>"
     * - Command:      "command.telegram.cammonitor.<botId>"
     */
    @Test
    fun `mapToBotId should return botId for notification routing key`() {
        val mapper = RoutingKeyBotIdMapper()

        assertEquals("stripchat", mapper.mapToBotId("notification.telegram.stripchat"))
        assertEquals("chaturbate", mapper.mapToBotId("notification.telegram.chaturbate"))
        assertEquals("myfreecams", mapper.mapToBotId("notification.telegram.myfreecams"))
    }

    @Test
    fun `mapToBotId should return botId for command routing key with cammonitor segment`() {
        val mapper = RoutingKeyBotIdMapper()

        assertEquals("stripchat", mapper.mapToBotId("command.telegram.cammonitor.stripchat"))
        assertEquals("chaturbate", mapper.mapToBotId("command.telegram.cammonitor.chaturbate"))
        assertEquals("myfreecams", mapper.mapToBotId("command.telegram.cammonitor.myfreecams"))
    }

    @Test
    fun `mapToBotId should return null when routingKey does not match expected pattern`() {
        val mapper = RoutingKeyBotIdMapper()

        assertNull(mapper.mapToBotId("invalid.routing.key"))
        assertNull(mapper.mapToBotId("telegram.bot123"))
        assertNull(mapper.mapToBotId(""))
    }

    /**
     * Tests for [RoutingKeyBotIdMapper.mapToRoutingKey]
     *
     * - NOTIFICATION → "notification.telegram.<botId>"
     * - COMMAND      → "command.telegram.<botId>"
     */
    @Test
    fun `mapToRoutingKey should return correctly formatted routingKey for NOTIFICATION type`() {
        val mapper = RoutingKeyBotIdMapper()

        assertEquals("notification.telegram.bot123", mapper.mapToRoutingKey("bot123", RoutingKeyType.NOTIFICATION))
    }

    @Test
    fun `mapToRoutingKey should return correctly formatted routingKey for COMMAND type`() {
        val mapper = RoutingKeyBotIdMapper()

        assertEquals("command.telegram.bot123", mapper.mapToRoutingKey("bot123", RoutingKeyType.COMMAND))
    }
}
