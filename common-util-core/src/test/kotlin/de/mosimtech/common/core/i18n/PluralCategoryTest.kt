package de.mosimtech.common.core.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

class PluralCategoryTest {

    @Test
    fun `German, English and Spanish distinguish one and other`() {
        listOf(AccountLanguage.GERMAN, AccountLanguage.ENGLISH, AccountLanguage.SPANISH).forEach { language ->
            assertEquals(PluralCategory.ONE, PluralCategory.of(language, 1))
            assertEquals(PluralCategory.OTHER, PluralCategory.of(language, 0))
            assertEquals(PluralCategory.OTHER, PluralCategory.of(language, 2))
            assertEquals(PluralCategory.OTHER, PluralCategory.of(language, 21))
        }
    }

    @Test
    fun `Russian and Ukrainian follow the CLDR integer rules`() {
        listOf(AccountLanguage.RUSSIAN, AccountLanguage.UKRAINIAN).forEach { language ->
            mapOf(
                1L to PluralCategory.ONE, 21L to PluralCategory.ONE, 101L to PluralCategory.ONE,
                2L to PluralCategory.FEW, 4L to PluralCategory.FEW, 23L to PluralCategory.FEW,
                0L to PluralCategory.MANY, 5L to PluralCategory.MANY, 11L to PluralCategory.MANY,
                12L to PluralCategory.MANY, 14L to PluralCategory.MANY, 111L to PluralCategory.MANY,
            ).forEach { (count, expected) ->
                assertEquals(expected, PluralCategory.of(language, count), "$language $count")
            }
        }
    }
}
