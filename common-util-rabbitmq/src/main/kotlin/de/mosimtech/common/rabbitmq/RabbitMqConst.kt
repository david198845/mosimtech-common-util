package de.mosimtech.common.rabbitmq

// All public and general constants for RabbitMQ are declared here

// Headers
const val X_AUTH_SERVICE_TOKEN = "X-Auth-Service-token"
const val X_AUTH_USER_TOKEN = "X-Auth-User-Token"

// Vhosts
const val MOMASOFT_VHOST = "momasoft"
const val SIMSTORE_VHOST = "simstore"
const val SHARED_VHOST = "shared"

const val NOTIFICATION_APP_QUEUE = "notification.app.queue"
const val NOTIFICATION_EMAIL_QUEUE = "notification.email.queue"
const val NOTIFICATION_TELEGRAM_QUEUE = "notification.telegram.queue"
const val COMMAND_TELEGRAM_QUEUE = "command.telegram.queue"

@Deprecated("Nutze NOTIFICATION_TELEGRAM_QUEUE stattdessen")
const val TELEGRAM_NOTIFICATION_QUEUE = "telegram.notification.queue"

// Exchanges
const val DOCUMENT_EXCHANGE = "document.upload.exchange"

@Deprecated("Use NOTIFICATION_EXCHANGE instead")
const val TELEGRAM_NOTIFICATION_EXCHANGE = "telegram.notification.exchange"

@Deprecated("Use COMMAND_TELEGRAM_QUEUE instead")
const val TELEGRAM_COMMAND_QUEUE = "telegram.command.queue"

@Deprecated("Use COMMAND_TELEGRAM_EXCHANGE instead")
const val TELEGRAM_COMMAND_EXCHANGE = "telegram.command.exchange"

const val NOTIFICATION_EXCHANGE = "notification.exchange"
const val COMMAND_TELEGRAM_EXCHANGE = "command.telegram.exchange"


// Routing Keys - Pattern: [domain].[channel].[subchannel]
const val DOCUMENT_CREATE_ROUTING_KEY = "document.create.routingkey"
const val DOCUMENT_UPDATE_ROUTING_KEY = "document.update.routingkey"
const val DOCUMENT_DELETE_ROUTING_KEY = "document.delete.routingkey"

const val NOTIFICATION_APP_ROUTING_KEY = "notification.app"
const val NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email"

const val NOTIFICATION_TELEGRAM_SC_ROUTING_KEY = "notification.telegram.stripchat"
const val NOTIFICATION_TELEGRAM_CB_ROUTING_KEY = "notification.telegram.chaturbate"
const val NOTIFICATION_TELEGRAM_MFC_ROUTING_KEY = "notification.telegram.myfreecams"

// Wildcards für die Listener (Die Raute '#' fängt alles was danach kommt)
const val NOTIFICATION_ALL_WILDCARD = "notification.#"
const val NOTIFICATION_TELEGRAM_ALL_WILDCARD = "notification.telegram.#"

// Commands
const val COMMAND_TELEGRAM_SC_ROUTING_KEY = "command.telegram.stripchat"
const val COMMAND_TELEGRAM_ALL_WILDCARD = "command.telegram.#"

// Deprecated Routing Keys (altes Schema: [channel].[domain].[subchannel].routingkey)
@Deprecated("Use NOTIFICATION_TELEGRAM_SC_ROUTING_KEY instead")
const val TELEGRAM_SC_NOTIFICATION_ROUTING_KEY = "telegram.notification.stripchat.routingkey"

@Deprecated("Use NOTIFICATION_TELEGRAM_CB_ROUTING_KEY instead")
const val TELEGRAM_CB_NOTIFICATION_ROUTING_KEY = "telegram.notification.chaturbate.routingkey"

@Deprecated("Use NOTIFICATION_TELEGRAM_MFC_ROUTING_KEY instead")
const val TELEGRAM_MFC_NOTIFICATION_ROUTING_KEY = "telegram.notification.myfreecams.routingkey"

@Deprecated("Use COMMAND_TELEGRAM_SC_ROUTING_KEY instead")
const val TELEGRAM_SC_COMMAND_ROUTING_KEY = "telegram.command.stripchat.routingkey"

@Deprecated("Use COMMAND_TELEGRAM_ALL_WILDCARD instead")
const val TELEGRAM_CB_COMMAND_ROUTING_KEY = "telegram.command.chaturbate.routingkey"

@Deprecated("Use COMMAND_TELEGRAM_ALL_WILDCARD instead")
const val TELEGRAM_MFC_COMMAND_ROUTING_KEY = "telegram.command.myfreecams.routingkey"

@Deprecated("Use COMMAND_TELEGRAM_ALL_WILDCARD instead")
const val TELEGRAM_COMMAND_WILDCARD_ROUTING_KEY = "telegram.command.*.routingkey"

@Deprecated("Use NOTIFICATION_TELEGRAM_ALL_WILDCARD instead")
const val TELEGRAM_NOTIFICATION_WILDCARD_ROUTING_KEY = "telegram.notification.*.routingkey"

@Deprecated("Use NOTIFICATION_APP_ROUTING_KEY instead")
const val APP_NOTIFICATION_ROUTING_KEY = "app.notification.routingkey"

@Deprecated("Use NOTIFICATION_EMAIL_ROUTING_KEY instead")
const val EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification.routingkey"

@Deprecated("Use NOTIFICATION_ALL_WILDCARD instead")
const val NOTIFICATION_WILDCARD_ROUTING_KEY = "notification.*.routingkey"
