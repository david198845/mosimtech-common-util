package de.mosimtech.common.core.dto.notification.common

/**
 * Represents the communication channels available for sending notifications.
 *
 * This enum defines a list of possible channels through which a notification can be
 * delivered to a recipient. It encapsulates the supported mechanisms for message delivery
 * in the application.
 *
 * Enum values:
 * - EMAIL: Represents notifications sent via email.
 * - SMS: Represents notifications sent via SMS (Short Message Service).
 * - PUSH: Represents notifications sent via push notifications.
 * - TELEGRAM: Represents notifications sent via Telegram messaging service.
 */
enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    TELEGRAM,

}
