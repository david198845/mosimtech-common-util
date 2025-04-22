# Datenbank-Integration Dokumentation

## Überblick

Die Datenbank-Integration der MoSimTech Common Utility Bibliothek bietet standardisierte Komponenten für die Interaktion mit verschiedenen Datenbanktypen. Sie unterstützt sowohl relationale Datenbanken (über JPA) als auch dokumentenorientierte Datenbanken (MongoDB) und implementiert einheitliche Muster für Datenbankoperationen.

## Hauptkomponenten

### URN-System

Das URN-System (Uniform Resource Name) ist ein zentraler Bestandteil der Datenbank-Integration und bietet einen einheitlichen Mechanismus zur Generierung eindeutiger Identifikatoren für Entitäten.

#### UrnNamespace Annotation

Die `@UrnNamespace` Annotation wird verwendet, um den Namespace für URN-Generierung zu definieren:

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UrnNamespace(
    val value: String,
    val subNamespaces: Array<String> = []
)
```

#### UrnBuilder

Die `UrnBuilder`-Klasse generiert eindeutige URNs basierend auf dem angegebenen Namespace:

```kotlin
object UrnBuilder {
    fun generateID(namespace: String, id: String = "", vararg subNamespaces: String): Urn {
        // Implementierung der URN-Generierung
    }
}
```

### JPA-Integration

Die JPA-Integration umfasst Komponenten für die Arbeit mit relationalen Datenbanken über die Java Persistence API.

#### UrnEntityListener

Der `UrnEntityListener` ist ein JPA-Entity-Listener, der automatisch URNs für neue Entitäten generiert:

```kotlin
class UrnEntityListener {
    @PrePersist
    fun onPrePersist(entity: Any) {
        // Generiert URNs für neue Entitäten vor dem Speichern
    }
}
```

Verwendung:

```kotlin
@Entity
@EntityListeners(UrnEntityListener::class)
@UrnNamespace("example")
class ExampleEntity {
    @Id
    var id: String? = null
    
    // Weitere Eigenschaften
}
```

### MongoDB-Integration

Die MongoDB-Integration bietet Komponenten für die Arbeit mit MongoDB-Dokumenten.

#### AbstractBaseEntity

Die `AbstractBaseEntity`-Klasse dient als Basisklasse für MongoDB-Entitäten:

```kotlin
abstract class AbstractBaseEntity {
    @Id
    var id: Urn? = null
    
    // Weitere gemeinsame Eigenschaften
}
```

#### MongoBeforeSaveListener

Der `MongoBeforeSaveListener` ist ein Event-Listener, der vor dem Speichern von MongoDB-Dokumenten ausgeführt wird und automatisch URNs für neue Entitäten generiert:

```kotlin
class MongoBeforeSaveListener : AbstractMongoEventListener<AbstractBaseEntity>() {
    override fun onBeforeConvert(event: BeforeConvertEvent<AbstractBaseEntity>) {
        val entity = event.getSource()
        if (entity.id == null || entity.id!!.isDefault()) {
            val annotation = entity.javaClass.getAnnotation(UrnNamespace::class.java)
                ?: throw IllegalStateException("Entity ${entity.javaClass.simpleName} must be annotated with @UrnNamespace")

            entity.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
        }
    }
}
```

Verwendung:

```kotlin
@Document
@UrnNamespace("example")
class ExampleDocument : AbstractBaseEntity() {
    var name: String = ""
    
    // Weitere Eigenschaften
}
```

## Verwendung

### Konfiguration der JPA-Integration

Um die JPA-Integration zu verwenden, konfigurieren Sie die Entity-Listener in Ihrer Spring-Konfiguration:

```kotlin
@Configuration
class JpaConfig {
    @Bean
    fun urnEntityListener(): UrnEntityListener {
        return UrnEntityListener()
    }
}
```

### Konfiguration der MongoDB-Integration

Für die MongoDB-Integration registrieren Sie den `MongoBeforeSaveListener`:

```kotlin
@Configuration
class MongoConfig {
    @Bean
    fun mongoBeforeSaveListener(): MongoBeforeSaveListener {
        return MongoBeforeSaveListener()
    }
}
```

### Erstellen einer JPA-Entität

```kotlin
@Entity
@EntityListeners(UrnEntityListener::class)
@UrnNamespace("product")
class Product {
    @Id
    var id: String? = null
    
    var name: String = ""
    var price: BigDecimal = BigDecimal.ZERO
    var description: String = ""
    
    // Weitere Eigenschaften und Methoden
}
```

### Erstellen eines MongoDB-Dokuments

```kotlin
@Document
@UrnNamespace("product")
class ProductDocument : AbstractBaseEntity() {
    var name: String = ""
    var price: BigDecimal = BigDecimal.ZERO
    var description: String = ""
    
    // Weitere Eigenschaften und Methoden
}
```

## Best Practices

### Namespace-Konventionen

Verwenden Sie konsistente Namespace-Konventionen für Ihre Entitäten. Empfohlene Praktiken:

- Verwenden Sie aussagekräftige, eindeutige Namespace-Namen
- Strukturieren Sie Namespaces hierarchisch (z.B. "momasoft:product")
- Dokumentieren Sie die verwendeten Namespaces zentral

### Auditing

Implementieren Sie Auditing für Ihre Entitäten, um Änderungen nachverfolgen zu können:

```kotlin
@Entity
@EntityListeners(value = [UrnEntityListener::class, AuditingEntityListener::class])
@UrnNamespace("product")
class Product {
    @Id
    var id: String? = null
    
    @CreatedDate
    var createdDate: LocalDateTime? = null
    
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
    
    @CreatedBy
    var createdBy: String? = null
    
    @LastModifiedBy
    var lastModifiedBy: String? = null
    
    // Weitere Eigenschaften
}
```

### Optimistische Sperrung

Verwenden Sie optimistische Sperrung, um Konflikte bei gleichzeitigen Änderungen zu vermeiden:

```kotlin
@Entity
@UrnNamespace("product")
class Product {
    @Id
    var id: String? = null
    
    @Version
    var version: Long = 0
    
    // Weitere Eigenschaften
}
```

## Fehlerbehebung

### Häufige Probleme

1. **Fehlende URN-Generierung**: Stellen Sie sicher, dass die Entität mit `@UrnNamespace` annotiert ist und der entsprechende Listener konfiguriert wurde.
2. **Duplizierte IDs**: Überprüfen Sie die Namespace-Konfiguration und stellen Sie sicher, dass die Namespaces eindeutig sind.
3. **Listener wird nicht ausgeführt**: Stellen Sie sicher, dass der Listener korrekt als Bean registriert ist und die Entität mit `@EntityListeners` annotiert ist.

## Zusammenfassung

Die Datenbank-Integration der MoSimTech Common Utility Bibliothek bietet eine flexible und erweiterbare Grundlage für die Arbeit mit verschiedenen Datenbanktypen. Durch die Verwendung des URN-Systems und standardisierter Listener wird eine konsistente Handhabung von Entitäten sichergestellt, während spezifische Anforderungen für verschiedene Datenbanktypen berücksichtigt werden.