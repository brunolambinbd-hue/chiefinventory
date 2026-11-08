package com.chefinventory.app.utils

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Registre data-driven alimenté depuis le fichier XML de test `src/test/resources/test_strings_en.xml`
 * fournissant les gabarits de messages d'assertions en Anglais Développeur.
 *
 * Conforme à la politique d'architecture [no_hardcoded_fallback_strings_policy.artifact.md] :
 * Aucune chaîne de secours en dur (*fallback string*) n'est autorisée.
 */
object OcrTestStrings {

    private val stringsMap: Map<String, String> by lazy {
        loadTestStringsXml()
    }

    val HEADER_EXPECTED: String get() = getValue("test_header_expected")
    val HEADER_ACTUAL: String get() = getValue("test_header_actual")

    val TITLE_MISMATCH: String get() = getValue("test_title_mismatch")
    val SERVINGS_MISMATCH: String get() = getValue("test_servings_mismatch")
    val WINE_VARIETAL_MISMATCH: String get() = getValue("test_wine_varietal_mismatch")
    val WINE_PRODUCER_MISMATCH: String get() = getValue("test_wine_producer_mismatch")
    val WINE_VINTAGE_MISMATCH: String get() = getValue("test_wine_vintage_mismatch")
    val WINE_VOLUME_MISMATCH: String get() = getValue("test_wine_volume_mismatch")

    val SOURCE_MISMATCH: String get() = getValue("test_source_mismatch")
    val PREP_TIME_MISMATCH: String get() = getValue("test_prep_time_mismatch")
    val COOK_TIME_MISMATCH: String get() = getValue("test_cook_time_mismatch")
    val RESTING_TIME_MISMATCH: String get() = getValue("test_resting_time_mismatch")
    val KCAL_MISMATCH: String get() = getValue("test_kcal_mismatch")
    val DIFFICULTY_MISMATCH: String get() = getValue("test_difficulty_mismatch")

    val INGREDIENTS_MISMATCH: String get() = getValue("test_ingredients_mismatch")
    val INSTRUCTIONS_MISMATCH: String get() = getValue("test_instructions_mismatch")
    val STEP4_MERGE_MISMATCH: String get() = getValue("test_step4_merge_mismatch")

    /**
     * Extrait une valeur de chaîne de test par sa clé [key] depuis [stringsMap].
     *
     * @param key La clé de ressource XML recherchée.
     * @return La valeur de la chaîne.
     * @throws IllegalStateException si la clé est absente du fichier `test_strings_en.xml` (Refus des Fallback Strings).
     */
    private fun getValue(key: String): String {
        return stringsMap[key] ?: error("Missing required test string resource key '$key' in test_strings_en.xml")
    }

    /**
     * Lit et parse le fichier `test_strings_en.xml` depuis le répertoire de ressources de tests.
     *
     * @return La carte des paires clé/valeur extraites du fichier XML.
     * @throws IllegalStateException si le fichier `test_strings_en.xml` est introuvable.
     */
    private fun loadTestStringsXml(): Map<String, String> {
        val inputStream: InputStream = OcrTestStrings::class.java.classLoader?.getResourceAsStream("test_strings_en.xml")
            ?: error("Could not locate required test resources file 'test_strings_en.xml' in test classpath")

        val map = mutableMapOf<String, String>()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(inputStream)
        doc.documentElement.normalize()

        val stringNodes = doc.getElementsByTagName("string")
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val elem = node as Element
                val stringName = elem.getAttribute("name")
                val rawText = elem.textContent.replace("\r", "").trim()
                if (stringName.isNotBlank()) {
                    map[stringName] = rawText.replace("\\'", "'")
                }
            }
        }
        return map
    }
}
