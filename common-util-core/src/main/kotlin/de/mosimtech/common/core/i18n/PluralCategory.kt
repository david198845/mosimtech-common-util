package de.mosimtech.common.core.i18n

import kotlin.math.abs

/**
 * Pluralkategorien nach CLDR für die unterstützten Kontosprachen, ganzzahlige Mengen.
 *
 * Deutsch, Englisch und Spanisch kennen nur `one` und `other`. Russisch und Ukrainisch
 * unterscheiden zusätzlich `few` (2–4, 22–24, …) und `many` (0, 5–20, 25–30, …).
 */
enum class PluralCategory(val suffix: String) {
    ONE("one"),
    FEW("few"),
    MANY("many"),
    OTHER("other");

    companion object {
        fun of(language: AccountLanguage, count: Long): PluralCategory {
            val n = abs(count)
            return when (language) {
                AccountLanguage.GERMAN, AccountLanguage.ENGLISH, AccountLanguage.SPANISH ->
                    if (n == 1L) ONE else OTHER

                AccountLanguage.RUSSIAN, AccountLanguage.UKRAINIAN -> slavic(n)
            }
        }

        /** Kategorien, die ein Katalog für die Sprache je Pluraltext enthalten muss. */
        fun requiredFor(language: AccountLanguage): Set<PluralCategory> = when (language) {
            AccountLanguage.GERMAN, AccountLanguage.ENGLISH, AccountLanguage.SPANISH -> setOf(ONE, OTHER)
            AccountLanguage.RUSSIAN, AccountLanguage.UKRAINIAN -> setOf(ONE, FEW, MANY, OTHER)
        }

        private fun slavic(n: Long): PluralCategory {
            val lastDigit = n % 10
            val lastTwo = n % 100
            return when {
                lastDigit == 1L && lastTwo != 11L -> ONE
                lastDigit in 2L..4L && lastTwo !in 12L..14L -> FEW
                else -> MANY
            }
        }
    }
}
