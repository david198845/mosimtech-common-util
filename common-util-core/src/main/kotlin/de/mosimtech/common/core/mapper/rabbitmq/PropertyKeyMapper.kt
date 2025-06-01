package de.mosimtech.common.core.mapper.rabbitmq

/**
 * A utility class for mapping property keys to a specific format.
 * It processes strings by manipulating segments separated by dots and combines them.
 */
class PropertyKeyMapper {

    /**
     * Transforms a string by splitting it at each dot (".") and combining the first two segments
     * with a dash ("-"). If there are fewer than two segments, returns the first segment.
     *
     * @param value The input string to be transformed.
     * @return A string formed by combining the first two segments with a dash,
     *         or the first segment if fewer than two segments exist.
     */
    fun map(value: String): String? {
        val parts = value.split(".")
        // Wir nehmen die ersten beiden Teile (falls vorhanden) und verbinden sie mit einem Bindestrich
        if(parts.any(String::isBlank)) return null
        return if (parts.size >= 2) {
            "${parts[0]}-${parts[1]}"
        } else {
            // Fallback für den Fall, dass es weniger als 2 Teile gibt
            parts[0]
        }
    }
}
