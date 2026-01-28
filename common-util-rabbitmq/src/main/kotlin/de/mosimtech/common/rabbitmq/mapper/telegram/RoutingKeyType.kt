package de.mosimtech.common.rabbitmq.mapper.telegram

enum class RoutingKeyType(val value: String) {

    NOTIFICATION("notification"),
    COMMAND("command");

}
