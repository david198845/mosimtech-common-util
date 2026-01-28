package de.mosimtech.common.rabbitmq

// All public and general constants for RabbitMQ are declared here

// Headers
const val X_AUTH_SERVICE_TOKEN = "X-Auth-Service-token"
const val X_AUTH_USER_TOKEN = "X-Auth-User-Token"

// Vhosts
const val MOMASOFT_VHOST = "momasoft"
const val SIMSTORE_VHOST = "simstore"
const val SHARED_VHOST = "shared"

// Queues
const val TELEGRAM_NOTIFICATION_QUEUE = "telegram.notification.queue"
const val TELEGRAM_COMMAND_QUEUE = "telegram.command.queue"
const val DOCUMENT_CREATE_QUEUE = "document.create.queue"


// Exchanges
const val DOCUMENT_EXCHANGE = "document.upload.exchange"
const val TELEGRAM_NOTIFICATION_EXCHANGE = "telegram.notification.exchange"
const val TELEGRAM_COMMAND_EXCHANGE = "telegram.command.exchange"


// Routing Keys
const val DOCUMENT_CREATE_ROUTING_KEY = "document.create.routingkey"
const val DOCUMENT_UPDATE_ROUTING_KEY = "document.update.routingkey"
const val DOCUMENT_DELETE_ROUTING_KEY = "document.delete.routingkey"
const val TELEGRAM_SC_NOTIFICATION_ROUTING_KEY = "telegram.notification.stripchat.routingkey"
const val TELEGRAM_CB_NOTIFICATION_ROUTING_KEY = "telegram.notification.chaturbate.routingkey"
const val TELEGRAM_MFC_NOTIFICATION_ROUTING_KEY = "telegram.notification.myfreecams.routingkey"
const val TELEGRAM_SC_COMMAND_ROUTING_KEY = "telegram.command.stripchat.routingkey"
const val TELEGRAM_CB_COMMAND_ROUTING_KEY = "telegram.command.chaturbate.routingkey"
const val TELEGRAM_MFC_COMMAND_ROUTING_KEY = "telegram.command.myfreecams.routingkey"

const val TELEGRAM_COMMAND_WILDCARD_ROUTING_KEY = "telegram.command.*.routingkey"
const val TELEGRAM_NOTIFICATION_WILDCARD_ROUTING_KEY = "telegram.notification.*.routingkey"
