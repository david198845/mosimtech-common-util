package de.mosimtech.common.core.dto.notification.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Unified notification request DTO for all notification channels.
 *
 * Sent over RabbitMQ to the appropriate notification service via routing key on [NOTIFICATION_EXCHANGE]:
 *   - "notification.push"     → Push Notification Service (Firebase/FCM)
 *   - "notification.telegram" → Telegram Notification Service
 *
 * Broadcast vs. targeted:
 *   - userId = null → broadcast to all subscribers of the application topic
 *   - userId = URN  → targeted notification to a specific user
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationRequestDTO(
    val application: String,
    val userId: String? = null,
    val title: String,
    val body: String,
    val type: NotificationType = NotificationType.INFO,
    val data: Map<String, String> = emptyMap(),
)
