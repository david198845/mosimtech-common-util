// Datei: IdGenerator.kt
package de.modulix.mosimtech.identifier

import java.util.*

/**
 * Utility object responsible for generating unique identifiers in the form of a URN (Uniform Resource Name).
 */
object IdGenerator {
    fun generateUrn(): String {
        return "urn:uuid:${UUID.randomUUID()}"
    }
}
