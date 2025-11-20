package de.mosimtech.common.core.dto.notification.common

/**
 * Represents the response data transfer object (DTO) for notifications.
 *
 * This class is designed to encapsulate a list of `NotificationResourceDTO` objects,
 * which detail individual notifications including their metadata and user-specific information.
 *
 * It serves as a response structure for APIs or methods that retrieve notification-related
 * data, providing a consistent format for notification-related information.
 *
 * Properties:
 * - notifications: A list of notifications represented as `NotificationResourceDTO`
 *   instances. Each entry contains detailed metadata for a specific notification.
 */
data class NotificationResponseDTO(
    val notifications: List<NotificationResourceDTO>
)
