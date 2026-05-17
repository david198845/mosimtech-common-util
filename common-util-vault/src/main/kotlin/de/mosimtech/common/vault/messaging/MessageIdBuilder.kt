package de.mosimtech.common.vault.messaging

import java.util.*

fun generateMessageId(context: String?): String {
    val prefix = context?.let { ":$it" } ?: ""
    return "urn:rabbitmq$prefix-${UUID.randomUUID()}"
}