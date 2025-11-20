package de.mosimtech.common.core.dto.notification.telegram
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.mosimtech.common.core.dto.notification.common.NotificationType
import de.mosimtech.common.core.urn.Urn

/**
 * Represents a request to send a notification.
 *
 * This class holds the details of the notification such as the message content,
 * the routing key to determine the delivery path, the notificationType indicating the severity
 * or category of the notification, and the identifier of the user
 * to whom the notification is targeted.
 *
 * @property message The content of the notification message.
 * @property routingKey The key used for routing the notification to the appropriate destination.
 * @property type The notificationType of the notification, indicating its level of importance or severity.
 * @property userId The unique identifier of the user to whom the notification is intended.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationRequestDTO(
    val message: String,
    val subject: String,
    val routingKey: String,
    val type: NotificationType,
    val userId: Urn,
    val channels: List<String> = emptyList()


)
