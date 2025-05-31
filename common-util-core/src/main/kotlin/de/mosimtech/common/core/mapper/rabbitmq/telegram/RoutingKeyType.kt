package de.mosimtech.common.core.mapper.rabbitmq.telegram

enum class RoutingKeyType(val value: String) {

    NOTIFICATION("notification"),
    COMMAND("command");

}
