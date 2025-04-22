# MoSimTech Common Utility - Erste Schritte

Diese Anleitung hilft Ihnen dabei, die MoSimTech Common Utility Bibliothek in Ihrem Projekt einzurichten und die grundlegenden Funktionen zu nutzen.

## Voraussetzungen

Bevor Sie beginnen, stellen Sie sicher, dass Sie folgende Voraussetzungen erfüllen:

- JDK 21 oder höher
- Gradle 8.x oder Maven 3.x
- Zugriff auf das MoSimTech Maven-Repository

## Installation

### Gradle (Kotlin DSL)

Fügen Sie das MoSimTech-Repository und die Abhängigkeit zu Ihrer `build.gradle.kts` Datei hinzu:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://192.168.2.33:9000/repository/maven-releases/")
        credentials {
            username = "username" // Ersetzen Sie dies durch Ihre Zugangsdaten
            password = "password" // Ersetzen Sie dies durch Ihre Zugangsdaten
        }
    }
}

dependencies {
    implementation("de.modulix.mosimtech:mosimtech-common-util:2.4.30")
}
```

### Maven

Fügen Sie das MoSimTech-Repository und die Abhängigkeit zu Ihrer `pom.xml` Datei hinzu:

```xml
<repositories>
    <repository>
        <id>mosimtech-repo</id>
        <url>https://192.168.2.33:9000/repository/maven-releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.modulix.mosimtech</groupId>
        <artifactId>mosimtech-common-util</artifactId>
        <version>2.4.30</version>
    </dependency>
</dependencies>
```

## Grundlegende Verwendung

### Erstellen eines DTOs

DTOs (Data Transfer Objects) sind ein zentraler Bestandteil der Bibliothek. Hier ist ein Beispiel für die Erstellung eines eigenen DTOs:

```kotlin
// Imports: de.modulix.mosimtech.dto.base.BaseDTO, java.time.LocalDateTime

class ProductDTO : BaseDTO {
    override val id: String? = null
    override val creationDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedBy: String? = null
    override val createdBy: String? = null
    override val userId: String? = null
    override val version: Long? = 1L
    override val valid: Boolean = true

    // Produktspezifische Eigenschaften
    var name: String = ""
    var description: String = ""
    var price: Double = 0.0
}
```

### Einrichten der JPA-Integration

Um die JPA-Integration zu verwenden, erstellen Sie eine Konfigurationsklasse und eine Entität:

```kotlin
// Imports: de.modulix.mosimtech.database.annotations.UrnNamespace, de.modulix.mosimtech.listener.jpa.UrnEntityListener,
// jakarta.persistence.Entity, jakarta.persistence.EntityListeners, jakarta.persistence.Id,
// org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration

@Configuration
class JpaConfig {
    @Bean
    fun urnEntityListener(): UrnEntityListener {
        return UrnEntityListener()
    }
}

@Entity
@EntityListeners(UrnEntityListener::class)
@UrnNamespace("product")
class Product {
    @Id
    var id: String? = null

    var name: String = ""
    var description: String = ""
    var price: Double = 0.0
}
```

### Einrichten der MongoDB-Integration

Für die MongoDB-Integration erstellen Sie eine Konfigurationsklasse und ein Dokument:

```kotlin
// Imports: de.modulix.mosimtech.database.annotations.UrnNamespace, de.modulix.mosimtech.database.mongodb.AbstractBaseEntity,
// de.modulix.mosimtech.listener.mongodb.MongoBeforeSaveListener, org.springframework.context.annotation.Bean,
// org.springframework.context.annotation.Configuration, org.springframework.data.mongodb.core.mapping.Document

@Configuration
class MongoConfig {
    @Bean
    fun mongoBeforeSaveListener(): MongoBeforeSaveListener {
        return MongoBeforeSaveListener()
    }
}

@Document
@UrnNamespace("product")
class ProductDocument : AbstractBaseEntity() {
    var name: String = ""
    var description: String = ""
    var price: Double = 0.0
}
```

### Einrichten der RabbitMQ-Integration

Für die RabbitMQ-Integration erstellen Sie eine Konfigurationsklasse:

```kotlin
// Imports: de.modulix.mosimtech.rabbitMQ.DOCUMENT_EXCHANGE, de.modulix.mosimtech.rabbitMQ.DOCUMENT_CREATED_ROUTING_KEY,
// org.springframework.amqp.core.*, org.springframework.amqp.rabbit.connection.ConnectionFactory,
// org.springframework.amqp.rabbit.core.RabbitTemplate, org.springframework.amqp.support.converter.Jackson2JsonMessageConverter,
// org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration

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
}
```

## Beispiel-Anwendungsfall

Hier ist ein vollständiges Beispiel für einen einfachen Anwendungsfall, der die verschiedenen Komponenten der Bibliothek verwendet:

### Service-Implementierung

```kotlin
// Imports: de.modulix.mosimtech.dto.base.BaseDTO, de.modulix.mosimtech.rabbitMQ.DOCUMENT_EXCHANGE,
// de.modulix.mosimtech.rabbitMQ.DOCUMENT_CREATED_ROUTING_KEY, de.modulix.mosimtech.rabbitMQ.X_AUTH_SERVICE_TOKEN,
// org.springframework.amqp.rabbit.core.RabbitTemplate, org.springframework.stereotype.Service,
// java.time.LocalDateTime

// DTO
class DocumentDTO : BaseDTO {
    override val id: String? = null
    override val creationDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedBy: String? = null
    override val createdBy: String? = null
    override val userId: String? = null
    override val version: Long? = 1L
    override val valid: Boolean = true

    var title: String = ""
    var content: String = ""
}

// Service
@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val rabbitTemplate: RabbitTemplate
) {
    fun createDocument(title: String, content: String): DocumentDTO {
        // Erstellen und Speichern des Dokuments
        val document = Document()
        document.title = title
        document.content = content

        val savedDocument = documentRepository.save(document)

        // Konvertieren in DTO
        val documentDTO = DocumentDTO()
        documentDTO.id = savedDocument.id
        documentDTO.title = savedDocument.title
        documentDTO.content = savedDocument.content

        // Ereignis senden
        rabbitTemplate.convertAndSend(
            DOCUMENT_EXCHANGE,
            DOCUMENT_CREATED_ROUTING_KEY,
            documentDTO,
            { message ->
                message.messageProperties.headers[X_AUTH_SERVICE_TOKEN] = "service-token"
                message
            }
        )

        return documentDTO
    }
}

// Listener
@Service
class DocumentEventListener {
    @RabbitListener(queues = ["document-created-queue"])
    fun handleDocumentCreated(document: DocumentDTO) {
        println("Dokument erstellt: ${document.title}")
        // Weitere Verarbeitung...
    }
}
```

## Nächste Schritte

Nachdem Sie die grundlegende Einrichtung abgeschlossen haben, können Sie die folgenden Ressourcen erkunden, um mehr über die Bibliothek zu erfahren:

- [Architekturübersicht](architecture-overview.md)
- [DTO-System Dokumentation](components/dto-system.md)
- [Datenbank-Integration Dokumentation](components/database-integration.md)
- [RabbitMQ-Integration Dokumentation](components/rabbitmq-integration.md)

## Fehlerbehebung

Wenn Sie auf Probleme stoßen, überprüfen Sie die folgenden häufigen Fehlerquellen:

1. **Abhängigkeitsprobleme**: Stellen Sie sicher, dass Sie die richtige Version der Bibliothek verwenden und alle erforderlichen Abhängigkeiten konfiguriert haben.
2. **Konfigurationsprobleme**: Überprüfen Sie Ihre Spring-Konfiguration und stellen Sie sicher, dass alle erforderlichen Beans korrekt definiert sind.
3. **Annotationsprobleme**: Stellen Sie sicher, dass alle erforderlichen Annotationen korrekt angewendet wurden, insbesondere `@UrnNamespace` für Entitäten.

Wenn Sie weitere Hilfe benötigen, wenden Sie sich an das MoSimTech-Entwicklungsteam.
