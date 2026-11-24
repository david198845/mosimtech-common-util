package de.mosimtech.common.delegation.event

import java.time.Instant

/**
 * Zentrale Konstanten für den RabbitMQ-Austausch von Delegations-Events.
 * Werden sowohl vom Publisher (user-service) als auch vom Consumer (shift-service) verwendet.
 */
@Deprecated("Ersatz: DelegationGrantChangedEvent")
object DelegationEventConstants {
    const val IAM_EVENTS_EXCHANGE = "iam.events.exchange"
    const val ROUTING_KEY_ACCEPTED = "delegation.accepted"
    const val ROUTING_KEY_REVOKED = "delegation.revoked"
}

@Deprecated("Ersatz: DelegationGrantChangedEvent")
enum class DelegationEventType {
    DELEGATION_ACCEPTED,
    DELEGATION_REVOKED
}

enum class DelegationModule {
    SHIFT_CALENDAR,
    FINANCE
}

@Deprecated("Ersatz: DelegationGrantChangedEvent")
data class DelegationGrantor(
    val userId: String,
    val name: String
)

@Deprecated("Ersatz: DelegationGrantChangedEvent")
data class DelegationGrantee(
    val userId: String,
    val email: String
)

@Deprecated("Ersatz: DelegationGrantChangedEvent")
data class DelegationEvent(
    val eventId: String,
    val timestamp: Instant,
    val eventType: DelegationEventType,
    val grantor: DelegationGrantor,
    val grantee: DelegationGrantee,
    val module: DelegationModule,
    val scopes: List<String>
)
