package de.mosimtech.common.rabbitmq.template

fun interface PublisherTemplate {
    fun sendMessage(vHost: String, exchangeKey: String, routingKey: String, payload: Any, bearerToken: String?)
}

