package de.modulix.mosimtech.rabbitMQ

abstract class RabbitMQPropertiesTemplate {
    open var vhosts: Map<String, RabbitMQConfigPropertyItem> = mutableMapOf()

    open class RabbitMQConfigPropertyItem {
        open lateinit var host: String
        open var port: Int = 5672
        open lateinit var username: String
        open lateinit var password: String // Passwort (über Vault integriert)
        open lateinit var virtualHost: String
        open var connectionTimeout: Int = 5000 // Optionaler Timeout-Wert
        open var exchange: Map<String, String> = mutableMapOf()
        open var queue: Map<String, String> = mutableMapOf()
        open var routingKey: Map<String, String> = mutableMapOf()
    }
}