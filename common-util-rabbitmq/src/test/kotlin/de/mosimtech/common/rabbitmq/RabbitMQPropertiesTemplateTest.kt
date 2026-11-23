package de.mosimtech.common.rabbitmq

import de.mosimtech.common.rabbitmq.RabbitMQPropertiesTemplate.RabbitMQConfigPropertyItem
import de.mosimtech.common.rabbitmq.RabbitMQPropertiesTemplate.RabbitMQSSLPropertiesTemplate
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.security.KeyStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RabbitMQPropertiesTemplateTest {

    private fun vhost(ssl: RabbitMQSSLPropertiesTemplate? = null) = RabbitMQConfigPropertyItem().apply {
        host = "rabbit.example"
        port = 5671
        username = "pellets"
        password = "secret"
        virtualHost = "shared"
        connectionTimeout = 1234
        this.ssl = ssl
    }

    @Test
    fun `connection factory carries the vhost settings`() {
        val factory = vhost().createConnectionFactory()

        assertEquals("rabbit.example", factory.host)
        assertEquals(5671, factory.port)
        assertEquals("shared", factory.virtualHost)
        assertEquals("pellets", factory.username)
        assertEquals("secret", factory.rabbitConnectionFactory.password)
        assertEquals(1234, factory.rabbitConnectionFactory.connectionTimeout)
        assertEquals(false, factory.rabbitConnectionFactory.isSSL)
    }

    @Test
    fun `ssl uses the client certificate from the key store`() {
        val dir = Files.createTempDirectory("rabbit-ssl").toFile()
        val storeFile = File(dir, "client.p12")
        storeFile.outputStream().use { KeyStore.getInstance("PKCS12").apply { load(null, null) }.store(it, "pw".toCharArray()) }
        val ssl = RabbitMQSSLPropertiesTemplate().apply {
            enabled = true
            key_store = "client.p12"
            key_store_path = dir.path
            key_store_password = "pw"
        }

        assertEquals("TLSv1.2", ssl.createSslContext().protocol)
        assertEquals(true, vhost(ssl).createConnectionFactory().rabbitConnectionFactory.isSSL)
    }

    @Test
    fun `disabled ssl ignores the key store and a missing one fails clearly`() {
        val ssl = RabbitMQSSLPropertiesTemplate().apply { key_store = "missing.p12"; key_store_path = "/nowhere" }
        assertEquals(false, vhost(ssl).createConnectionFactory().rabbitConnectionFactory.isSSL)

        ssl.enabled = true
        assertFailsWith<FileNotFoundException> { vhost(ssl).createConnectionFactory() }
    }
}
