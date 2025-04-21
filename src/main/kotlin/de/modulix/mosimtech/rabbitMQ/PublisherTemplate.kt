package de.modulix.mosimtech.rabbitMQ

import org.springframework.amqp.rabbit.core.RabbitTemplate

abstract class PublisherTemplate(
    protected val rabbitTemplates: Map<String, RabbitTemplate>,
    protected val rabbitMQProperties: RabbitMQPropertiesTemplate
) {

    protected fun sendMessage(
        vHost: String,
        exchangeKey: String,
        routingKey: String,
        payload: Any,
        bearerToken: String
    ) {
        val rabbitTemplate = rabbitTemplates[vHost]
            ?: throw IllegalArgumentException("Unbekannter VHost: $vHost")

        val vHostConfig = rabbitMQProperties.vhosts[vHost]
            ?: throw IllegalArgumentException("Konfiguration für VHost '$vHost' nicht gefunden")

        val exchange = vHostConfig.exchange[exchangeKey]
            ?: throw IllegalArgumentException("Exchange '$exchangeKey' für VHost '$vHost' nicht gefunden")

        val routingKeyValue = vHostConfig.routingKey[routingKey]
            ?: throw IllegalArgumentException("Routing Key '$routingKey' für VHost '$vHost' nicht gefunden")

        rabbitTemplate.convertAndSend(exchange, routingKeyValue, payload) {
            it.messageProperties.headers["Authorization"] = bearerToken
            it
        }
    }

}