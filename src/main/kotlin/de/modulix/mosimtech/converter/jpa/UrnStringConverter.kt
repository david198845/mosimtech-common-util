package de.modulix.mosimtech.converter.jpa

import de.modulix.mosimtech.database.base.urn.Urn
import jakarta.persistence.AttributeConverter


/**
 * UrnConverter is an abstract class providing functionalities to convert URN (Uniform Resource Name)
 * objects to their string representation suitable for database storage and back to URN objects.
 */
abstract class UrnStringConverter : AttributeConverter<Urn?, String?> {


    /**
     * Converts a URN object to its string representation suitable for database storage.
     *
     * @param attribute The URN object to be converted. Can be null.
     * @return The string representation of the URN suitable for database storage, or null if the input URN is null.
     */
    override fun convertToDatabaseColumn(attribute: Urn?): String? {
        return attribute?.toUrnString()
    }

    /**
     * Converts a string representation of a URN (Uniform Resource Name) from the database to an Urn object.
     *
     * @param dbData The string representation of the URN from the database. Can be null.
     * @return An instance of the Urn class if the string can be successfully parsed, or null if the input is null or invalid.
     */
    override fun convertToEntityAttribute(dbData: String?): Urn? {
        return dbData?.let { Urn.parse(it) }
    }
}