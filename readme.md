# MoSimTech Common Utility Bibliothek

Diese Bibliothek ist die Grundstruktur für die Entwicklung von Microservices für die Projekte MoMaSoft und SimStor sowie
alle weiteren zukünftigen Projekte. Sie enthält die grundlegenden Funktionen und Klassen, die für die Entwicklung von
Programmen erforderlich sind. Die Bibliothek ist in verschiedene Module unterteilt und bietet eine konsistente Basis für
Microservice-Architekturen.

## Überblick

Die MoSimTech Common Utility Bibliothek (Version 2.4.30) ist eine umfassende Sammlung von Hilfsfunktionen, Basisklassen und
Integrationen für moderne Microservice-Anwendungen. Sie unterstützt verschiedene Datenbanktechnologien, Messaging-Systeme
und bietet standardisierte Komponenten für häufig benötigte Funktionalitäten.

## Hauptkomponenten

Die Bibliothek ist in folgende Hauptkomponenten unterteilt:

- **DTO (Data Transfer Objects)**: Standardisierte Objekte für den Datenaustausch zwischen Services
- **Datenbank-Integration**: Unterstützung für JPA, MongoDB und andere Datenbanksysteme
- **Event Listener**: Automatisierte Verarbeitung von Datenbank- und Anwendungsereignissen
- **RabbitMQ-Integration**: Messaging-Infrastruktur für die Kommunikation zwischen Microservices
- **Sicherheitskomponenten**: Authentifizierung und Autorisierung für Microservices
- **Serialisierung**: Angepasste Serialisierungsfunktionen für verschiedene Datenformate
- **Builder und Converter**: Hilfsfunktionen für die Erstellung und Konvertierung von Objekten

## Technologien

Die Bibliothek basiert auf folgenden Technologien:

- **Kotlin 2.1.x**: Moderne, typsichere JVM-Sprache
- **Spring Framework**: Spring Boot, Spring Data, Spring Security
- **Messaging**: RabbitMQ für asynchrone Kommunikation
- **Datenbanken**: JPA/Hibernate, MongoDB
- **Validierung**: Jakarta Validation API
- **Serialisierung**: Jackson für JSON/CSV
- **Geldwerte**: JavaMoney/Moneta

## Funktionen und Klassen

- **Basis Klassen**: 
  - BaseDTO: Grundlegende Schnittstelle für alle DTOs
  - AbstractBaseEntity: Basisklasse für Datenbankentitäten
  - Verschiedene spezialisierte DTOs für unterschiedliche Anwendungsfälle

- **Event Listener**:
  - JPA Entity Listener für automatische ID-Generierung und Auditing
  - MongoDB Event Listener für Dokument-Lifecycle-Management

- **RabbitMQ-Integration**:
  - Vorkonfigurierte Exchanges und Queues
  - Standardisierte Header und Routing-Keys
  - Unterstützung für verschiedene Virtual Hosts (MoMaSoft, SimStore, Shared)

- **URN-System**:
  - Einheitliches System zur Generierung eindeutiger Identifikatoren
  - Namespace-basierte ID-Struktur für verschiedene Entitätstypen

## Installation

Hier finden Sie die Schritte zur Installation und Integration der Bibliothek in Ihr Projekt.

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://192.168.2.33:9000/repository/maven-releases/")
        credentials {
            username = "username"
            password = "password"
        }
    }
}

dependencies {
    implementation("de.modulix.mosimtech:mosimtech-common-util:2.4.30")
}
```

### Maven

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

## Verwendung

Nach der Installation können Sie die Komponenten der Bibliothek in Ihrem Projekt verwenden:

```kotlin
// Beispiel für die Verwendung eines BaseDTO
class MyDTO : BaseDTO {
    override val id: String? = null
    override val creationDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedDate: LocalDateTime? = LocalDateTime.now()
    override val lastModifiedBy: String? = "system"
    override val createdBy: String? = "system"
    override val userId: String? = null
    override val version: Long? = 1L
    override val valid: Boolean = true

    // Eigene Eigenschaften
    var name: String = ""
    var description: String = ""
}
```

## Dokumentation

Detaillierte Dokumentation zu allen Komponenten finden Sie im `docs`-Verzeichnis. Die Dokumentation umfasst:

- Architekturübersicht
- Komponentenbeschreibungen
- Anwendungsbeispiele
- Best Practices

## Lizenz

Diese Bibliothek wird unter der GNU General Public License (GPL) veröffentlicht. Weitere Informationen finden Sie in der
LICENSE Datei.
