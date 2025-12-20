package de.mosimtech.common.mongo.converter.mongodb

import de.mosimtech.common.core.urn.Urn
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class UrnToStringConverter : Converter<Urn, String> {
    override fun convert(source: Urn): String {
        return source.toUrnString()
    }
}