package de.modulix.mosimtech.dto.notification.telegram

/**
 * Represents the type of a notification, indicating its level of importance or severity.
 *
 * Types available:
 * - INFO: Represents an informational message that is not critical.
 * - WARNING: Indicates a warning that may require attention.
 * - ERROR: Represents a critical error or issue that requires immediate action.
 */
enum class Type {
    INFO,
    WARNING,
    ERROR
}