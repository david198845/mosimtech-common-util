package de.modulix.mosimtech.converter

import de.modulix.mosimtech.model.Urn

abstract class UrnConverter {

    fun convertToDatabaseColumn(attribute: Urn?): String? {
        return attribute?.toUrnString()
    }

    fun convertToEntityAttribute(dbData: String?): Urn? {
        return dbData?.let { Urn.parse(it) }
    }
}