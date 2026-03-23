package de.mosimtech.common.rabbitmq

// All public and general constants for RabbitMQ are declared here.
// REGEL: Hier stehen NUR noch globale Infrastruktur-Teile (Vhosts, Headers, Exchanges).

// ==========================================
// 1. HEADERS & VHOSTS (Global)
// ==========================================
const val X_AUTH_SERVICE_TOKEN = "X-Auth-Service-token"
const val X_AUTH_USER_TOKEN = "X-Auth-User-Token"

const val MOMASOFT_VHOST = "momasoft"
const val SIMSTORE_VHOST = "simstore"
const val SHARED_VHOST = "shared"

// ==========================================
// 2. EXCHANGES / POSTÄMTER (Global)
// ==========================================
const val DOCUMENT_EXCHANGE = "document.upload.exchange"
const val NOTIFICATION_EXCHANGE = "notification.exchange"
const val COMMAND_TELEGRAM_EXCHANGE = "command.telegram.exchange"


// ==========================================
// 3. DEPRECATED / ALTLASTEN 
// (Bleiben für Rückwärtskompatibilität, bis die alten Services abgeschaltet sind)
// ==========================================
@Deprecated("Nutze NOTIFICATION_TELEGRAM_QUEUE stattdessen lokal im Service")
const val TELEGRAM_NOTIFICATION_QUEUE = "telegram.notification.queue"

@Deprecated("Use NOTIFICATION_EXCHANGE instead")
const val TELEGRAM_NOTIFICATION_EXCHANGE = "telegram.notification.exchange"

@Deprecated("Use lokale Command Queue instead")
const val TELEGRAM_COMMAND_QUEUE = "telegram.command.queue"

@Deprecated("Use COMMAND_TELEGRAM_EXCHANGE instead")
const val TELEGRAM_COMMAND_EXCHANGE = "telegram.command.exchange"

@Deprecated("Use NOTIFICATION_TELEGRAM_SC_ROUTING_KEY lokal instead")
const val TELEGRAM_SC_NOTIFICATION_ROUTING_KEY = "telegram.notification.stripchat.routingkey"

@Deprecated("Use NOTIFICATION_TELEGRAM_CB_ROUTING_KEY lokal instead")
const val TELEGRAM_CB_NOTIFICATION_ROUTING_KEY = "telegram.notification.chaturbate.routingkey"

@Deprecated("Use NOTIFICATION_TELEGRAM_MFC_ROUTING_KEY lokal instead")
const val TELEGRAM_MFC_NOTIFICATION_ROUTING_KEY = "telegram.notification.myfreecams.routingkey"

@Deprecated("Use lokales Command Routing lokal instead")
const val TELEGRAM_SC_COMMAND_ROUTING_KEY = "telegram.command.stripchat.routingkey"

@Deprecated("Use lokales Command Routing lokal instead")
const val TELEGRAM_CB_COMMAND_ROUTING_KEY = "telegram.command.chaturbate.routingkey"

@Deprecated("Use lokales Command Routing lokal instead")
const val TELEGRAM_CS_COMMAND_ROUTING_KEY = "telegram.command.camsoda.routingkey"

@Deprecated("Use lokales Command Routing lokal instead")
const val TELEGRAM_MFC_COMMAND_ROUTING_KEY = "telegram.command.myfreecams.routingkey"

@Deprecated("Use lokales Wildcard lokal instead")
const val TELEGRAM_COMMAND_WILDCARD_ROUTING_KEY = "telegram.command.*.routingkey"

@Deprecated("Use lokales Wildcard lokal instead")
const val TELEGRAM_NOTIFICATION_WILDCARD_ROUTING_KEY = "telegram.notification.*.routingkey"

@Deprecated("Use lokales Routing lokal instead")
const val APP_NOTIFICATION_ROUTING_KEY = "app.notification.routingkey"

@Deprecated("Use lokales Routing lokal instead")
const val EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification.routingkey"

@Deprecated("Use lokales Wildcard lokal instead")
const val NOTIFICATION_WILDCARD_ROUTING_KEY = "notification.*.routingkey"


// ====================================================================================
// MIGRATION CHEAT SHEET - AUSLAGERUNG IN LOKALE SERVICES
// Diese Konstanten wurden aus der Common-Library entfernt, da sie service-spezifisch sind.
// Bitte kopiere sie bei Bedarf direkt in die jeweiligen Services (Sender/Empfänger).
// ====================================================================================

/*

// --- GEHÖRT IN DEN TELEGRAM-SERVICE (Und in die Sender-Services im Backend) ---

// Queues
// const val NOTIFICATION_TELEGRAM_QUEUE = "notification.telegram.queue"

// Routing Keys (Notifications)
// const val NOTIFICATION_TELEGRAM_SC_ROUTING_KEY = "notification.telegram.stripchat"
// const val NOTIFICATION_TELEGRAM_CB_ROUTING_KEY = "notification.telegram.chaturbate"
// const val NOTIFICATION_TELEGRAM_MFC_ROUTING_KEY = "notification.telegram.myfreecams"

// Wildcards (Listener)
// const val NOTIFICATION_ALL_WILDCARD = "notification.#"
// const val NOTIFICATION_TELEGRAM_ALL_WILDCARD = "notification.telegram.#"


// --- GEHÖRT IN DEN CAMMONITOR-SERVICE (Und in den Telegram-Service zum Senden) ---

// Queues
// const val COMMAND_CAMMONITOR_TELEGRAM_QUEUE = "command.telegram.cammonitor.queue"

// Routing Keys (Commands) - Hinweis: Doppelpunkt bei SC wurde korrigiert
// const val COMMAND_TELEGRAM_SC_ROUTING_KEY = "command.telegram.cammonitor.stripchat"
// const val COMMAND_TELEGRAM_CB_ROUTING_KEY = "command.telegram.cammonitor.chaturbate"
// const val COMMAND_TELEGRAM_CS_ROUTING_KEY = "command.telegram.cammonitor.camsoda" 
// const val COMMAND_TELEGRAM_MFC_ROUTING_KEY = "command.telegram.cammonitor.myfreecams"

// Wildcards (Listener)
// const val COMMAND_TELEGRAM_ALL_WILDCARD = "command.telegram.cammonitor.#"


// --- GEHÖRT IN APP- / EMAIL-SERVICES ---
// const val NOTIFICATION_APP_QUEUE = "notification.app.queue"
// const val NOTIFICATION_EMAIL_QUEUE = "notification.email.queue"
// const val NOTIFICATION_APP_ROUTING_KEY = "notification.app"
// const val NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email"


// --- GEHÖRT IN DEN DOCUMENT-SERVICE ---
// const val DOCUMENT_CREATE_ROUTING_KEY = "document.create.routingkey"
// const val DOCUMENT_UPDATE_ROUTING_KEY = "document.update.routingkey"
// const val DOCUMENT_DELETE_ROUTING_KEY = "document.delete.routingkey"

*/