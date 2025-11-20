package de.mosimtech.common.core.dto.notification.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents a data transfer object (DTO) for requesting the creation or dispatch of a notification.
 *
 * This class is used to encapsulate the information required to send or process
 * a notification in the system. It includes details such as the application sending
 * the notification, the message content, subject, notification type, user identifier,
 * and the channels through which the notification should be delivered.
 *
 * Properties:
 * - application: The name or identifier of the application initiating the notification.
 * - message: The content or body of the notification message.
 * - subject: The subject or title of the notification.
 * - type: The type or category of the notification, represented by the `NotificationType` enum.
 * - userId: The identifier of the user to whom the notification is intended.
 * - channels: A list of communication channels, determined by the `NotificationChannel` enum,
 *   through which the notification will be delivered. Defaults to an empty list.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationRequestDTO(
    val application: String,
    val message: String,
    val subject: String,
    val type: NotificationType,
    val userId: String,
    val channels: List<NotificationChannel> = emptyList()
) {
}
