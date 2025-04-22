# MoSimTech Common Utility - Architekturübersicht

## Einführung

Die MoSimTech Common Utility Bibliothek dient als Grundlage für die Entwicklung von Microservices in den Projekten MoMaSoft und SimStor. Diese Dokumentation bietet einen Überblick über die Architektur der Bibliothek und erklärt, wie die verschiedenen Komponenten zusammenarbeiten.

## Architekturprinzipien

Die Bibliothek folgt diesen grundlegenden Architekturprinzipien:

1. **Modularität**: Jede Komponente ist so gestaltet, dass sie unabhängig verwendet werden kann.
2. **Wiederverwendbarkeit**: Gemeinsame Funktionalitäten sind in wiederverwendbaren Komponenten gekapselt.
3. **Standardisierung**: Einheitliche Muster und Konventionen für konsistente Implementierungen.
4. **Erweiterbarkeit**: Einfache Erweiterung durch neue Funktionalitäten ohne Änderung bestehender Komponenten.

## Schichtenarchitektur

Die Bibliothek ist in folgende Schichten organisiert:

### 1. Basiskomponenten
- **DTO (Data Transfer Objects)**: Standardisierte Objekte für den Datenaustausch
- **Entitäten**: Basisklassen für Datenbankentitäten
- **Serialisierung**: Komponenten für die Umwandlung von Objekten in verschiedene Formate

### 2. Datenbankintegration
- **JPA-Integration**: Komponenten für relationale Datenbanken
- **MongoDB-Integration**: Komponenten für dokumentenorientierte Datenbanken
- **Event Listener**: Automatisierte Verarbeitung von Datenbankereignissen

### 3. Messaging
- **RabbitMQ-Integration**: Komponenten für asynchrone Kommunikation
- **Event-Handling**: Standardisierte Verarbeitung von Ereignissen

### 4. Sicherheit
- **Authentifizierung**: Komponenten für die Benutzerauthentifizierung
- **Autorisierung**: Komponenten für die Zugriffssteuerung

## Komponentenübersicht

### DTO-System

Das DTO-System bildet das Fundament für den Datenaustausch zwischen Services. Es basiert auf dem `BaseDTO`-Interface, das grundlegende Eigenschaften wie ID, Erstellungs- und Änderungsdaten, Benutzerinformationen und Versionierung definiert.

```
BaseDTO
  ├── DocumentDTO
  ├── UserProfileDTO
  └── [weitere spezialisierte DTOs]
```

### Datenbank-Integration

Die Datenbank-Integration umfasst Komponenten für verschiedene Datenbanktypen:

```
Datenbank-Integration
  ├── JPA
  │   ├── UrnEntityListener
  │   └── [weitere JPA-Komponenten]
  ├── MongoDB
  │   ├── MongoBeforeSaveListener
  │   └── [weitere MongoDB-Komponenten]
  └── URN-System
      ├── UrnBuilder
      ├── UrnNamespace
      └── [weitere URN-Komponenten]
```

### Messaging-System

Das Messaging-System basiert auf RabbitMQ und ermöglicht die asynchrone Kommunikation zwischen Microservices:

```
Messaging-System
  ├── RabbitMQPropertiesTemplate
  ├── RabbitMqConst
  └── [weitere Messaging-Komponenten]
```

## Interaktionen zwischen Komponenten

Die Komponenten interagieren auf folgende Weise:

1. **DTO ↔ Entitäten**: DTOs werden verwendet, um Daten zwischen Services zu übertragen, während Entitäten für die Persistenz in Datenbanken verwendet werden.
2. **Event Listener ↔ Datenbanken**: Event Listener reagieren auf Datenbankereignisse und führen automatisierte Aktionen aus.
3. **Messaging ↔ Services**: Das Messaging-System ermöglicht die asynchrone Kommunikation zwischen verschiedenen Microservices.

## Erweiterungspunkte

Die Bibliothek bietet folgende Erweiterungspunkte:

1. **Neue DTO-Typen**: Erweiterung des DTO-Systems durch neue spezialisierte DTOs.
2. **Zusätzliche Event Listener**: Implementierung neuer Event Listener für spezifische Anwendungsfälle.
3. **Neue Messaging-Ereignisse**: Definition neuer Ereignistypen für die Kommunikation zwischen Services.

## Fazit

Die MoSimTech Common Utility Bibliothek bietet eine solide Grundlage für die Entwicklung von Microservices. Durch die modulare Architektur und die standardisierten Komponenten wird die Entwicklung konsistenter und wartbarer Anwendungen erleichtert.