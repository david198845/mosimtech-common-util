package de.mosimtech.common.rabbitmq.template

import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers

interface ListenerTemplate {

    fun receiveMessage(payload: Any, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) routingKey: String)

    fun receiveMessage(payload: Any, @Headers headers: Map<String, Any>)

}
