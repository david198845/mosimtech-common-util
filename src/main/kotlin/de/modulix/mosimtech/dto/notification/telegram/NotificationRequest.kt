package de.modulix.mosimtech.dto.notification.telegram
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.modulix.mosimtech.database.urn.Urn

/**
 * Represents a request to send a notification.
 *
 * This class holds the details of the notification such as the message content,
 * the routing key to determine the delivery path, the type indicating the severity
 * or category of the notification, and the identifier of the user
 * to whom the notification is targeted.
 *
 * @property message The content of the notification message.
 * @property routingKey The key used for routing the notification to the appropriate destination.
 * @property type The type of the notification, indicating its level of importance or severity.
 * @property userId The unique identifier of the user to whom the notification is intended.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationRequest(
    val message: String,
    val routingKey: String,
    val type: Type,
    val userId: Urn
)