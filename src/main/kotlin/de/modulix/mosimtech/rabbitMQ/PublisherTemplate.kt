package de.modulix.mosimtech.rabbitMQ

import org.springframework.amqp.rabbit.core.RabbitTemplate

interface PublisherTemplate {

    fun sendMessage(vHost: String, exchangeKey: String, routingKey: String, payload: Any, bearerToken: String)


}