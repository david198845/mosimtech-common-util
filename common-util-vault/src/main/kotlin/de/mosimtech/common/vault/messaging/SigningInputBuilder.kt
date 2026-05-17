package de.mosimtech.common.vault.messaging

import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

// Base64-Kodierung des Payloads macht den |-Delimiter eindeutig, unabhängig vom Payload-Inhalt.
internal fun <T> buildSigningInput(
    objectMapper: ObjectMapper,
    payload: T,
    issuedAt: Instant,
    exp: Instant?,
    messageId: String,
    serviceAccountToken: String,
    userToken: String?,
): ByteArray {
    val payloadBase64 = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(payload))
    return "$payloadBase64|$issuedAt|${exp ?: "∞"}|$messageId|$serviceAccountToken|${userToken ?: ""}".toByteArray(
        Charsets.UTF_8
    )
}
