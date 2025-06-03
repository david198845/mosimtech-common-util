package de.mosimtech.common.core.rabbitMQ

fun interface PublisherTemplate {
    fun sendMessage(vHost: String, exchangeKey: String, routingKey: String, payload: Any, bearerToken: String?)
}

