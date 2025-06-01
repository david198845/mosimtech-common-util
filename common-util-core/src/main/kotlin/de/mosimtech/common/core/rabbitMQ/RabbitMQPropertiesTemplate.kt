package de.mosimtech.common.core.rabbitMQ

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
        open var ssl: RabbitMQSSLPropertiesTemplate? = null
    }

    open class RabbitMQSSLPropertiesTemplate {
        open var enabled: Boolean = false
        open var protocol: String = "TLSv1.2"
        open var key_store: String = ""
        open var key_store_type: String = "PKCS12"
        open var key_store_path: String = ""
        open var key_store_password: String = ""
        open var trust_store: String = ""
        open var trust_store_type: String = "PKCS12"
        open var trust_store_path: String = ""
        open var trust_store_password: String = ""
        open var validate_server_certificate: Boolean = true
    }

}

