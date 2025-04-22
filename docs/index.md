# MoSimTech Common Utility - Dokumentation

Willkommen zur Dokumentation der MoSimTech Common Utility Bibliothek. Diese Dokumentation bietet umfassende Informationen zur Verwendung und Integration der Bibliothek in Ihre Projekte.

## Inhaltsverzeichnis

### Einführung

- [Erste Schritte](getting-started.md) - Schnelleinstieg in die Verwendung der Bibliothek
- [Architekturübersicht](architecture-overview.md) - Überblick über die Architektur der Bibliothek

### Komponenten

- [DTO-System](components/dto-system.md) - Dokumentation des Data Transfer Object Systems
- [Datenbank-Integration](components/database-integration.md) - Dokumentation der Datenbankintegration (JPA, MongoDB)
- [RabbitMQ-Integration](components/rabbitmq-integration.md) - Dokumentation der RabbitMQ-Messaging-Integration

## Über die Bibliothek

Die MoSimTech Common Utility Bibliothek ist die Grundstruktur für die Entwicklung von Microservices für die Projekte MoMaSoft und SimStor sowie alle weiteren zukünftigen Projekte. Sie enthält die grundlegenden Funktionen und Klassen, die für die Entwicklung von Programmen erforderlich sind.

### Hauptmerkmale

- **Standardisierte DTOs**: Einheitliche Struktur für Datentransferobjekte
- **Datenbank-Integration**: Unterstützung für JPA und MongoDB mit automatischer ID-Generierung
- **Messaging-System**: RabbitMQ-Integration für asynchrone Kommunikation
- **Sicherheitskomponenten**: Authentifizierung und Autorisierung für Microservices

### Version

Die aktuelle Version der Bibliothek ist 2.4.30.

## Unterstützte Technologien

- **Kotlin 2.1.x**: Moderne, typsichere JVM-Sprache
- **Spring Framework**: Spring Boot, Spring Data, Spring Security
- **Messaging**: RabbitMQ für asynchrone Kommunikation
- **Datenbanken**: JPA/Hibernate, MongoDB
- **Validierung**: Jakarta Validation API
- **Serialisierung**: Jackson für JSON/CSV
- **Geldwerte**: JavaMoney/Moneta

## Lizenz

Diese Bibliothek wird unter der GNU General Public License (GPL) veröffentlicht. Weitere Informationen finden Sie in der LICENSE Datei.