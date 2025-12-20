package de.mosimtech.common.mongo.converter.mongodb

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class StringToUrnConverter : Converter<String, Urn> {
    override fun convert(source: String): Urn {
        return source.toUrn()!!
    }
}