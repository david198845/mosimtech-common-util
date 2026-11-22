package de.mosimtech.common.core.i18n

import java.util.Properties

/**
 * Prüft einen Textkatalog über alle [AccountLanguage]s gegen die deutsche Vorgabe.
 * Gedacht für einen Test je Dienst: `assertEquals(emptyList(), texts.problems())`.
 *
 * Gefunden wird:
 * - eine fehlende Sprachdatei,
 * - fehlende oder überzählige Schlüssel,
 * - abweichende Platzhalter (`{0}`, `{1}`, …),
 * - Pluraltexte ohne die Kategorien, die die Sprache verlangt.
 */
object LocalizedCatalogCheck {

    private val PLACEHOLDER = Regex("""\{(\d+)[^}]*}""")
    private val PLURAL_SUFFIXES = PluralCategory.entries.map { ".${it.suffix}" }

    fun problems(baseName: String, classLoader: ClassLoader): List<String> {
        val reference = load(baseName, null, classLoader)
            ?: return listOf("$baseName.properties fehlt")
        val referenceKeys = singularKeys(reference)
        val referencePlurals = pluralStems(reference)

        return AccountLanguage.entries.flatMap { language ->
            val file = fileName(baseName, language)
            val catalog = if (language == AccountLanguage.DEFAULT) reference
            else load(baseName, language, classLoader) ?: return@flatMap listOf("$file fehlt")
            checkLanguage(file, language, reference, referenceKeys, referencePlurals, catalog)
        }
    }

    private fun checkLanguage(
        file: String,
        language: AccountLanguage,
        reference: Properties,
        referenceKeys: Set<String>,
        referencePlurals: Set<String>,
        catalog: Properties,
    ): List<String> = buildList {
        val keys = singularKeys(catalog)
        val plurals = pluralStems(catalog)
        (referenceKeys - keys).sorted().forEach { add("$file: Schlüssel fehlt: $it") }
        (keys - referenceKeys).sorted().forEach { add("$file: Schlüssel zu viel: $it") }
        (referencePlurals - plurals).sorted().forEach { add("$file: Pluraltext fehlt: $it") }
        (plurals - referencePlurals).sorted().forEach { add("$file: Pluraltext zu viel: $it") }

        (referenceKeys intersect keys).sorted().forEach { key ->
            if (placeholders(reference.getProperty(key)) != placeholders(catalog.getProperty(key))) {
                add("$file: Platzhalter weichen ab: $key")
            }
        }
        (referencePlurals intersect plurals).sorted().forEach { stem ->
            PluralCategory.requiredFor(language)
                .filterNot { catalog.containsKey("$stem.${it.suffix}") }
                .forEach { add("$file: Pluralform ${it.suffix} fehlt: $stem") }
        }
    }

    private fun singularKeys(properties: Properties): Set<String> =
        properties.stringPropertyNames().filterNot(::isPluralKey).toSet()

    private fun pluralStems(properties: Properties): Set<String> =
        properties.stringPropertyNames().filter(::isPluralKey).map { it.substringBeforeLast('.') }.toSet()

    private fun isPluralKey(key: String): Boolean = PLURAL_SUFFIXES.any(key::endsWith)

    private fun placeholders(pattern: String): Set<String> =
        PLACEHOLDER.findAll(pattern).map { it.groupValues[1] }.toSet()

    private fun fileName(baseName: String, language: AccountLanguage?): String =
        if (language == null || language == AccountLanguage.DEFAULT) "$baseName.properties"
        else "${baseName}_${language.code}.properties"

    private fun load(baseName: String, language: AccountLanguage?, classLoader: ClassLoader): Properties? =
        classLoader.getResourceAsStream(fileName(baseName, language))?.use { stream ->
            Properties().apply { load(stream.reader(Charsets.UTF_8)) }
        }
}
