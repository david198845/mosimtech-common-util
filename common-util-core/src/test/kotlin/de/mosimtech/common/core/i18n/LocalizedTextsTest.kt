package de.mosimtech.common.core.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalizedTextsTest {
    private val texts = LocalizedTexts("i18n/test-texts", javaClass.classLoader)

    @Test
    fun `falls back to German for an unknown language`() {
        assertEquals("Hallo Ada", texts.forLanguage(null).text("greeting", "Ada"))
    }

    @Test
    fun `does not group numbers passed as parameters`() {
        assertEquals("Jahr 2026", texts.forLanguage(AccountLanguage.GERMAN).text("year", 2026))
    }

    @Test
    fun `chooses the plural form of the language`() {
        val russian = texts.forLanguage(AccountLanguage.RUSSIAN)
        assertEquals("1 день", russian.plural("days", 1))
        assertEquals("3 дня", russian.plural("days", 3))
        assertEquals("5 дней", russian.plural("days", 5))
        assertEquals("1 Tag", texts.forLanguage(AccountLanguage.GERMAN).plural("days", 1))
        assertEquals("2 Tage", texts.forLanguage(AccountLanguage.GERMAN).plural("days", 2))
    }

    @Test
    fun `reports an inconsistent catalog`() {
        val problems = LocalizedTexts("i18n/broken-texts", javaClass.classLoader).problems()

        assertTrue("i18n/broken-texts_en.properties: Schlüssel fehlt: farewell" in problems, problems.toString())
        assertTrue("i18n/broken-texts_en.properties: Platzhalter weichen ab: greeting" in problems, problems.toString())
        assertTrue("i18n/broken-texts_ru.properties: Pluralform few fehlt: days" in problems, problems.toString())
        assertTrue("i18n/broken-texts_es.properties fehlt" in problems, problems.toString())
    }

    @Test
    fun `accepts a consistent catalog`() {
        assertEquals(emptyList(), texts.problems())
    }
}
