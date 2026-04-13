package de.mosimtech.common.delegation.event

import java.time.Instant

enum class DelegationEventType {
    DELEGATION_ACCEPTED,
    DELEGATION_REVOKED
}

enum class DelegationModule {
    SHIFT_CALENDAR,
    FINANCE
}

data class DelegationGrantor(
    val userId: String,
    val name: String
)

data class DelegationGrantee(
    val userId: String,
    val email: String
)

data class DelegationEvent(
    val eventId: String,
    val timestamp: Instant,
    val eventType: DelegationEventType,
    val grantor: DelegationGrantor,
    val grantee: DelegationGrantee,
    val module: DelegationModule,
    val scopes: List<String>
)
