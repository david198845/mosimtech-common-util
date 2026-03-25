package de.mosimtech.common.core.dto.notification.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.mosimtech.common.core.urn.Urn

/**
 * Unified notification request DTO for all notification channels.
 *
 * Sent over RabbitMQ to the appropriate notification service via routing key on [NOTIFICATION_EXCHANGE]:
 *   - "notification.push"     → Push Notification Service (Firebase/FCM)
 *   - "notification.telegram" → Telegram Notification Service
 *
 * Broadcast vs. targeted:
 *   - userId = null → broadcast to all subscribers of the application topic
 *   - userId = Urn  → targeted notification to a specific user
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationRequestDTO(
    val application: String,
    val userId: Urn? = null,
    val title: String,
    val body: String,
    val type: NotificationType = NotificationType.INFO,
    val data: Map<String, String> = emptyMap(),
    val channelId: String? = null,
    val collapseKey: String? = null,
    val priority: String? = null,
    val dataOnly: Boolean = false,
    val icon: String? = null,
    val color: String? = null
)
