package de.mosimtech.common.core.rabbitmq

import de.mosimtech.common.core.mapper.rabbitmq.PropertyKeyMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class PropertyKeyMapperTest {

    @Test
    fun `map should return concatenated first and second parts with hyphen if value contains at least two parts`() {
        // Arrange
        val mapper = PropertyKeyMapper()
        val input = "part1.part2.part3"
        val expected = "part1-part2"

        // Act
        val result = mapper.map(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `map should return the first part if value contains only one part`() {
        // Arrange
        val mapper = PropertyKeyMapper()
        val input = "singlePart"
        val expected = "singlePart"

        // Act
        val result = mapper.map(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `map should return concatenated first and second parts with hyphen for a value with exactly two parts`() {
        // Arrange
        val mapper = PropertyKeyMapper()
        val input = "part1.part2"
        val expected = "part1-part2"

        // Act
        val result = mapper.map(input)

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `map should return an empty string if the input is empty`() {
        // Arrange
        val mapper = PropertyKeyMapper()
        val input = ""

        // Act
        val result = mapper.map(input)

        // Assert
        assertNull(result)
    }

    @Test
    fun `map should handle strings with only dots`() {
        // Arrange
        val mapper = PropertyKeyMapper()
        val input = "..."

        // Act
        val result = mapper.map(input)

        // Assert
        assertNull(result)
    }
}
