package de.mosimtech.common.vault.messaging

import tools.jackson.databind.ObjectMapper
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/**
 * Präfix des Hash-Segments. Der Doppelpunkt kommt im Base64-Alphabet nicht vor — eine
 * [PayloadSigningMode.FULL]-Eingabe kann deshalb nie mit einer [PayloadSigningMode.DIGEST]-Eingabe
 * übereinstimmen, auch nicht für einen anderen Payload.
 */
internal const val DIGEST_SEGMENT_PREFIX = "sha256:"

// Base64-Kodierung des Payloads macht den |-Delimiter eindeutig, unabhängig vom Payload-Inhalt.
internal fun <T> buildSigningInput(
    objectMapper: ObjectMapper,
    payload: T,
    issuedAt: Instant,
    exp: Instant?,
    messageId: String,
    serviceAccountToken: String,
    userToken: String?,
    mode: PayloadSigningMode = PayloadSigningMode.FULL,
): ByteArray = buildSigningInput(
    objectMapper.writeValueAsBytes(payload), issuedAt, exp, messageId, serviceAccountToken, userToken, mode
)

internal fun buildSigningInput(
    payloadBytes: ByteArray,
    issuedAt: Instant,
    exp: Instant?,
    messageId: String,
    serviceAccountToken: String,
    userToken: String?,
    mode: PayloadSigningMode,
): ByteArray {
    val payloadSegment = when (mode) {
        PayloadSigningMode.FULL -> Base64.getEncoder().encodeToString(payloadBytes)
        PayloadSigningMode.DIGEST -> DIGEST_SEGMENT_PREFIX + sha256Hex(payloadBytes)
    }
    return "$payloadSegment|$issuedAt|${exp ?: "∞"}|$messageId|$serviceAccountToken|${userToken ?: ""}".toByteArray(
        Charsets.UTF_8
    )
}

private fun sha256Hex(bytes: ByteArray): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
