package de.modulix.mosimtech.rabbitMQ

// All public and general constants for RabbitMQ are declared here

// Headers
const val X_AUTH_SERVICE_TOKEN = "X-Auth-Service-token"
const val X_AUTH_USER_TOKEN = "X-Auth-User-Token"

// Vhosts
const val MOMASOFT_VHOST = "momasoft"
const val SIMSTORE_VHOST = "simstore"
const val SHARED_VHOST = "shared"

// Queues


// Exchanges
const val DOCUMENT_EXCHANGE = "document"


// Routing Keys
const val DOCUMENT_CREATED_ROUTING_KEY = "document-created"
const val DOCUMENT_UPDATED_ROUTING_KEY = "document-updated"
const val DOCUMENT_DELETED_ROUTING_KEY = "document-deleted"