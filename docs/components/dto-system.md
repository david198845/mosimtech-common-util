# DTO-System Dokumentation

## Überblick

Das DTO-System (Data Transfer Object) ist eine zentrale Komponente der MoSimTech Common Utility Bibliothek. Es bietet standardisierte Objekte für den Datenaustausch zwischen verschiedenen Microservices und Anwendungsschichten. DTOs kapseln Daten und trennen die Datenübertragungslogik von der Geschäftslogik.

## Hauptkomponenten

### BaseDTO Interface

Das `BaseDTO` Interface definiert die grundlegende Struktur für alle DTOs in der Anwendung. Es enthält gemeinsame Eigenschaften, die für die meisten Datenübertragungsobjekte relevant sind.

```kotlin
interface BaseDTO : Serializable {
    val id: String?
    val creationDate: LocalDateTime?
    val lastModifiedDate: LocalDateTime?
    val lastModifiedBy: String?
    val createdBy: String?
    val userId: String?
    val version: Long?
    val valid: Boolean

    fun setInvalid() {
        this.valid = false
    }
}
```

#### Eigenschaften

- **id**: Eindeutiger Identifikator des Objekts
- **creationDate**: Zeitpunkt der Erstellung
- **lastModifiedDate**: Zeitpunkt der letzten Änderung
- **lastModifiedBy**: Benutzer, der die letzte Änderung vorgenommen hat
- **createdBy**: Benutzer, der das Objekt erstellt hat
- **userId**: Benutzer-ID, die mit diesem Objekt verknüpft ist
- **version**: Versionsnummer für optimistische Sperrung
- **valid**: Gibt an, ob das Objekt gültig ist

#### Methoden

- **setInvalid()**: Markiert das Objekt als ungültig

### Spezialisierte DTOs

Basierend auf dem `BaseDTO` Interface wurden verschiedene spezialisierte DTOs für unterschiedliche Anwendungsfälle implementiert:

#### DocumentDTO

Repräsentiert Dokumente im System mit zusätzlichen dokumentspezifischen Eigenschaften.

#### UserProfileDTO

Enthält Benutzerprofilinformationen und wird für die Benutzerauthentifizierung und -verwaltung verwendet.

## Verwendung

### Implementierung eines eigenen DTO

Um ein eigenes DTO zu erstellen, implementieren Sie das `BaseDTO` Interface und fügen Sie Ihre spezifischen Eigenschaften hinzu:

```kotlin
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
    val name: String = ""
    val price: BigDecimal = BigDecimal.ZERO
    val description: String = ""
    val category: String = ""
}
```

### Konvertierung zwischen DTO und Entity

Für die Konvertierung zwischen DTOs und Entitäten können Sie Konverter-Klassen oder Erweiterungsfunktionen verwenden:

```kotlin
// Beispiel für eine Erweiterungsfunktion zur Konvertierung
fun Product.toDTO(): ProductDTO {
    return ProductDTO().apply {
        id = this@toDTO.id
        name = this@toDTO.name
        price = this@toDTO.price
        description = this@toDTO.description
        category = this@toDTO.category
    }
}

fun ProductDTO.toEntity(): Product {
    return Product().apply {
        id = this@toEntity.id
        name = this@toEntity.name
        price = this@toEntity.price
        description = this@toEntity.description
        category = this@toEntity.category
    }
}
```

## Best Practices

### Immutabilität

Es wird empfohlen, DTOs so weit wie möglich unveränderlich (immutable) zu gestalten, um unerwartete Änderungen zu vermeiden. Verwenden Sie `val` anstelle von `var` für Eigenschaften, die nach der Erstellung nicht mehr geändert werden sollten.

### Validierung

Implementieren Sie Validierungslogik für Ihre DTOs, um sicherzustellen, dass die Daten den Anforderungen entsprechen. Sie können die Jakarta Validation API mit Annotationen verwenden:

```kotlin
class ProductDTO : BaseDTO {
    // BaseDTO-Implementierung...
    
    @NotBlank(message = "Der Produktname darf nicht leer sein")
    val name: String = ""
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Der Preis muss größer oder gleich 0 sein")
    val price: BigDecimal = BigDecimal.ZERO
    
    val description: String = ""
    
    @NotBlank(message = "Die Kategorie darf nicht leer sein")
    val category: String = ""
}
```

### Serialisierung

Achten Sie darauf, dass Ihre DTOs korrekt serialisiert und deserialisiert werden können. Die Bibliothek verwendet Jackson für die JSON-Serialisierung. Bei Bedarf können Sie benutzerdefinierte Serialisierer implementieren:

```kotlin
@JsonSerialize(using = CustomDateSerializer::class)
@JsonDeserialize(using = CustomDateDeserializer::class)
val specialDate: LocalDateTime? = null
```

## Fehlerbehebung

### Häufige Probleme

1. **Zirkuläre Abhängigkeiten**: Vermeiden Sie zirkuläre Abhängigkeiten zwischen DTOs, da diese zu Problemen bei der Serialisierung führen können.
2. **Fehlende Eigenschaften**: Stellen Sie sicher, dass alle erforderlichen Eigenschaften des `BaseDTO` Interfaces implementiert sind.
3. **Serialisierungsprobleme**: Bei Problemen mit der Serialisierung überprüfen Sie die Jackson-Konfiguration und implementieren Sie bei Bedarf benutzerdefinierte Serialisierer.

## Zusammenfassung

Das DTO-System der MoSimTech Common Utility Bibliothek bietet eine flexible und erweiterbare Grundlage für den Datenaustausch zwischen verschiedenen Komponenten. Durch die Verwendung des `BaseDTO` Interfaces wird eine konsistente Struktur für alle DTOs sichergestellt, während spezialisierte DTOs spezifische Anforderungen erfüllen können.