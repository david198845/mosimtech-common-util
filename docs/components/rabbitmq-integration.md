# RabbitMQ-Integration Dokumentation

## Überblick

Die RabbitMQ-Integration der MoSimTech Common Utility Bibliothek bietet standardisierte Komponenten für die asynchrone Kommunikation zwischen Microservices. Sie implementiert ein einheitliches Messaging-System basierend auf RabbitMQ, das die Entkopplung von Services und die ereignisgesteuerte Architektur unterstützt.

## Hauptkomponenten

### RabbitMqConst

Die `RabbitMqConst`-Klasse definiert Konstanten für die RabbitMQ-Konfiguration, einschließlich Header, Virtual Hosts, Exchanges und Routing Keys:

```kotlin
// Headers
const val X_AUTH_SERVICE_TOKEN = "X-Auth-Service-token"
const val X_AUTH_USER_TOKEN = "X-Auth-User-Token"

// Vhosts
const val MOMASOFT_VHOST = "momasoft"
const val SIMSTORE_VHOST = "simstore"
const val SHARED_VHOST = "shared"

// Exchanges
const val DOCUMENT_EXCHANGE = "document"

// Routing Keys
const val DOCUMENT_CREATED_ROUTING_KEY = "document-created"
const val DOCUMENT_UPDATED_ROUTING_KEY = "document-updated"
const val DOCUMENT_DELETED_ROUTING_KEY = "document-deleted"
```

### RabbitMQPropertiesTemplate

Die `RabbitMQPropertiesTemplate`-Klasse bietet Vorlagen für die Konfiguration von RabbitMQ-Verbindungen und -Nachrichten:

```kotlin
class RabbitMQPropertiesTemplate {
    // Konfigurationsvorlagen für RabbitMQ
}
```

## Virtual Hosts

Die Bibliothek unterstützt mehrere Virtual Hosts für verschiedene Anwendungsbereiche:

### MOMASOFT_VHOST

Der `momasoft` Virtual Host ist für die MoMaSoft-spezifische Kommunikation vorgesehen.

### SIMSTORE_VHOST

Der `simstore` Virtual Host ist für die SimStore-spezifische Kommunikation vorgesehen.

### SHARED_VHOST

Der `shared` Virtual Host ist für die gemeinsame Kommunikation zwischen verschiedenen Anwendungen vorgesehen.

## Exchanges und Routing Keys

### Document Exchange

Der `document` Exchange ist für die Verarbeitung von Dokumentenereignissen konfiguriert:

- **document-created**: Routing Key für Ereignisse zur Dokumenterstellung
- **document-updated**: Routing Key für Ereignisse zur Dokumentaktualisierung
- **document-deleted**: Routing Key für Ereignisse zur Dokumentlöschung

## Verwendung

### Konfiguration von RabbitMQ

Um RabbitMQ in Ihrer Anwendung zu konfigurieren, erstellen Sie eine Konfigurationsklasse:

```kotlin
@Configuration
class RabbitMQConfig {
    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = Jackson2JsonMessageConverter()
        return template
    }
    
    @Bean
    fun documentExchange(): Exchange {
        return ExchangeBuilder
            .directExchange(DOCUMENT_EXCHANGE)
            .durable(true)
            .build()
    }
    
    @Bean
    fun documentCreatedQueue(): Queue {
        return QueueBuilder
            .durable("document-created-queue")
            .build()
    }
    
    @Bean
    fun documentCreatedBinding(documentCreatedQueue: Queue, documentExchange: Exchange): Binding {
        return BindingBuilder
            .bind(documentCreatedQueue)
            .to(documentExchange)
            .with(DOCUMENT_CREATED_ROUTING_KEY)
            .noargs()
    }
    
    // Weitere Queue- und Binding-Konfigurationen
}
```

### Senden von Nachrichten

Um Nachrichten über RabbitMQ zu senden, verwenden Sie den `RabbitTemplate`:

```kotlin
@Service
class DocumentService(private val rabbitTemplate: RabbitTemplate) {
    fun createDocument(document: DocumentDTO) {
        // Dokument erstellen
        
        // Ereignis senden
        rabbitTemplate.convertAndSend(
            DOCUMENT_EXCHANGE,
            DOCUMENT_CREATED_ROUTING_KEY,
            document,
            { message ->
                message.messageProperties.headers[X_AUTH_SERVICE_TOKEN] = "service-token"
                message
            }
        )
    }
}
```

### Empfangen von Nachrichten

Um Nachrichten zu empfangen, implementieren Sie einen `@RabbitListener`:

```kotlin
@Service
class DocumentEventListener {
    @RabbitListener(queues = ["document-created-queue"])
    fun handleDocumentCreated(document: DocumentDTO) {
        // Verarbeitung des erstellten Dokuments
    }
    
    @RabbitListener(queues = ["document-updated-queue"])
    fun handleDocumentUpdated(document: DocumentDTO) {
        // Verarbeitung des aktualisierten Dokuments
    }
    
    @RabbitListener(queues = ["document-deleted-queue"])
    fun handleDocumentDeleted(document: DocumentDTO) {
        // Verarbeitung des gelöschten Dokuments
    }
}
```

## Authentifizierung und Autorisierung

Die RabbitMQ-Integration unterstützt die Authentifizierung und Autorisierung durch spezielle Header:

- **X_AUTH_SERVICE_TOKEN**: Token für die Service-zu-Service-Authentifizierung
- **X_AUTH_USER_TOKEN**: Token für die Benutzerauthentifizierung

Beispiel für die Verwendung von Authentifizierungsheadern:

```kotlin
rabbitTemplate.convertAndSend(
    DOCUMENT_EXCHANGE,
    DOCUMENT_CREATED_ROUTING_KEY,
    document,
    { message ->
        message.messageProperties.headers[X_AUTH_SERVICE_TOKEN] = serviceToken
        message.messageProperties.headers[X_AUTH_USER_TOKEN] = userToken
        message
    }
)
```

## Best Practices

### Nachrichtenstruktur

Verwenden Sie standardisierte DTOs für Nachrichten, um die Konsistenz zu gewährleisten:

```kotlin
data class EventMessage<T>(
    val eventType: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val payload: T,
    val metadata: Map<String, Any> = emptyMap()
)
```

### Fehlerbehandlung

Implementieren Sie eine robuste Fehlerbehandlung für Nachrichtenverarbeitung:

```kotlin
@RabbitListener(queues = ["document-created-queue"])
fun handleDocumentCreated(document: DocumentDTO) {
    try {
        // Verarbeitung des Dokuments
    } catch (e: Exception) {
        // Fehlerbehandlung
        logger.error("Fehler bei der Verarbeitung des Dokuments: ${e.message}", e)
        // Eventuell Nachricht in eine Dead-Letter-Queue verschieben
    }
}
```

### Dead Letter Queues

Konfigurieren Sie Dead Letter Queues für nicht verarbeitbare Nachrichten:

```kotlin
@Bean
fun documentCreatedQueue(): Queue {
    return QueueBuilder
        .durable("document-created-queue")
        .withArgument("x-dead-letter-exchange", "dead-letter-exchange")
        .withArgument("x-dead-letter-routing-key", "document-created-dead-letter")
        .build()
}

@Bean
fun deadLetterExchange(): Exchange {
    return ExchangeBuilder
        .directExchange("dead-letter-exchange")
        .durable(true)
        .build()
}

@Bean
fun documentCreatedDeadLetterQueue(): Queue {
    return QueueBuilder
        .durable("document-created-dead-letter-queue")
        .build()
}

@Bean
fun documentCreatedDeadLetterBinding(
    documentCreatedDeadLetterQueue: Queue,
    deadLetterExchange: Exchange
): Binding {
    return BindingBuilder
        .bind(documentCreatedDeadLetterQueue)
        .to(deadLetterExchange)
        .with("document-created-dead-letter")
        .noargs()
}
```

## Fehlerbehebung

### Häufige Probleme

1. **Verbindungsprobleme**: Überprüfen Sie die RabbitMQ-Verbindungseinstellungen und stellen Sie sicher, dass der RabbitMQ-Server läuft.
2. **Fehlende Exchanges oder Queues**: Stellen Sie sicher, dass alle erforderlichen Exchanges und Queues korrekt deklariert sind.
3. **Serialisierungsprobleme**: Überprüfen Sie, ob der richtige MessageConverter konfiguriert ist und die DTOs serialisierbar sind.
4. **Authentifizierungsfehler**: Stellen Sie sicher, dass die erforderlichen Authentifizierungsheader korrekt gesetzt sind.

## Zusammenfassung

Die RabbitMQ-Integration der MoSimTech Common Utility Bibliothek bietet eine flexible und erweiterbare Grundlage für die asynchrone Kommunikation zwischen Microservices. Durch die Verwendung standardisierter Exchanges, Queues und Routing Keys wird eine konsistente Handhabung von Ereignissen sichergestellt, während spezifische Anforderungen für verschiedene Anwendungsbereiche berücksichtigt werden.