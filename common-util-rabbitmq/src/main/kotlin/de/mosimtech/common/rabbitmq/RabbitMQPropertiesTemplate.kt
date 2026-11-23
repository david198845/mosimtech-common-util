package de.mosimtech.common.rabbitmq

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

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

        /**
         * Eine [CachingConnectionFactory] für diesen vHost: Host, Port, vHost, Zugangsdaten und
         * [connectionTimeout]; mit `ssl.enabled` zusätzlich das Client-Zertifikat aus
         * [RabbitMQSSLPropertiesTemplate.createSslContext]. Weitere Einstellungen (Publisher
         * Confirms, Returns) setzt der Aufrufer auf dem Ergebnis.
         */
        fun createConnectionFactory(): CachingConnectionFactory =
            CachingConnectionFactory(host, port).also { factory ->
                factory.virtualHost = virtualHost
                factory.username = username
                factory.setPassword(password)
                factory.setConnectionTimeout(connectionTimeout)
                ssl?.takeIf { it.enabled }?.let { factory.rabbitConnectionFactory.useSslProtocol(it.createSslContext()) }
            }
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

        /**
         * [SSLContext] mit dem Client-Zertifikat aus `<key_store_path>/<key_store>` (Typ
         * [key_store_type], Vorgabe PKCS12) – wie bisher in den Diensten. Der Server wird gegen die
         * Vertrauensanker der JVM geprüft; `trust_store*` und [validate_server_certificate] werden
         * hier (noch) nicht ausgewertet.
         */
        fun createSslContext(): SSLContext {
            val password = key_store_password.toCharArray()
            val keyStore = KeyStore.getInstance(key_store_type)
            FileInputStream("$key_store_path/$key_store").use { keyStore.load(it, password) }
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, password)
            return SSLContext.getInstance(protocol).apply { init(keyManagerFactory.keyManagers, null, null) }
        }
    }

}
