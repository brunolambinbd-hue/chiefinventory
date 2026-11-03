package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Chargeur data-driven responsable de l'ingestion, du parsing XML et de la mise en cache
 * du vocabulaire métier de l'application [OcrVocabulary].
 *
 * Responsabilités :
 * - Localiser et lire le répertoire `res/values` du projet depuis le disque (mode TDD/JVM) ou les ressources Android.
 * - Parser les fichiers XML (`ocr_*.xml`, `step_keywords.xml`, `ingredients.xml`, `wine_arrays.xml`) sans dépendance Android Framework.
 * - Effectuer la mise en cache de l'instance [OcrVocabulary] par défaut pour accélérer l'exécution des tests.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrVocabularyLoader {
    private const val TAG = "OcrVocabularyLoader"

    @Volatile
    private var cachedDefaultVocabulary: OcrVocabulary? = null

    /**
     * Charge le vocabulaire métier complet depuis les fichiers XML du répertoire `res/values`.
     *
     * @param projectDir Répertoire racine du projet (par défaut : dossier courant `"."`).
     * @return L'objet [OcrVocabulary] alimenté.
     * @throws IllegalStateException si le répertoire `res/values` est introuvable.
     */
    fun loadFromProjectResources(projectDir: File = File(".")): OcrVocabulary {
        if ((projectDir.path == ".") && (cachedDefaultVocabulary != null)) {
            return cachedDefaultVocabulary!!
        }

        val resDir = findResValuesDir(projectDir)
        if (resDir == null) {
            val errorMsg = "Could not locate 'res/values' directory from: ${projectDir.absolutePath}"
            LogManager.e(TAG, errorMsg)
            throw IllegalStateException(errorMsg)
        }

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        parseResDirectory(resDir, stringArrays, strings)

        val vocabulary = OcrVocabulary(
            stepActionKeywords = getList(stringArrays, "step_action_keywords"),
            commonIngredientsNoQty = getList(stringArrays, "common_ingredients_no_qty"),
            excludedOcrKeywords = getList(stringArrays, "excluded_ocr_keywords"),
            ingredientPreparationModifiers = getList(stringArrays, "ingredient_preparation_modifiers"),
            instructionSwitchKeywords = getList(stringArrays, "instruction_switch_keywords"),
            ingredientSemanticExclusions = getList(stringArrays, "ingredient_semantic_exclusions"),
            ingredientEndMarkers = getList(stringArrays, "ingredient_end_markers"),
            ingredientSubHeaders = getList(stringArrays, "ingredient_sub_headers"),
            sourceKeywords = getList(stringArrays, "source_keywords"),
            knownSourceBrands = getList(stringArrays, "known_source_brands"),
            phonePrefixes = getList(stringArrays, "phone_prefixes"),
            wineAppellations = getList(stringArrays, "wine_appellations"),
            wineProducers = getList(stringArrays, "wine_producers"),
            wineKeywords = getList(stringArrays, "wine_keywords"),
            wineTitleKeywords = getList(stringArrays, "wine_title_keywords"),
            ocr3ExplicitWineHeaders = getList(stringArrays, "ocr3_explicit_wine_headers"),
            wineRemovePattern = getStringVal(strings, "wine_remove_pattern"),
            ocrSpellingRepairs = getList(stringArrays, "ocr_spelling_repairs"),
            adExclusions = getList(stringArrays, "advertisement_exclusions"),
            ocrNoiseExclusions = getList(stringArrays, "ocr_noise_exclusions"),
            titleKeywords = getList(stringArrays, "title_keywords"),
            mandatoryComplementIngredients = getList(stringArrays, "ingredient_mandatory_complements"),
            hangingConnectors = getList(stringArrays, "hanging_connectors"),
            ingredientVarietyQualifiers = getList(stringArrays, "ingredient_variety_qualifiers"),
            baseIngredientsExpectingVariety = getList(stringArrays, "base_ingredients_expecting_variety"),
            standaloneArticles = getList(stringArrays, "standalone_articles"),
            culinaryMeasurementUnits = getList(stringArrays, "culinary_measurement_units"),
            titleSpellingRepairs = getList(stringArrays, "title_spelling_repairs"),
            titleMultilineConnectors = getList(stringArrays, "title_multiline_connectors"),
            ingredientSplitterConnectors = getList(stringArrays, "ingredient_splitter_connectors"),
            ocrDiacriticRepairs = getList(stringArrays, "ocr_diacritic_repairs"),
            priceTitleKeywords = getList(stringArrays, "price_title_keywords"),
            priceCategoriesLow = getList(stringArrays, "price_categories_low"),
            priceCategoriesMedium = getList(stringArrays, "price_categories_medium"),
            priceCategoriesHigh = getList(stringArrays, "price_categories_high"),
            ingredientConnectors = getList(stringArrays, "ingredient_connectors"),
            protectedSectionHeaders = getList(stringArrays, "ocr_protected_section_headers"),
            timeHeaderPrefixes = getList(stringArrays, "ocr_time_header_prefixes"),
            timeHeaderKeywords = getList(stringArrays, "ocr_time_header_keywords"),
        )

        cacheVocabularyIfNeeded(projectDir, vocabulary)
        return vocabulary
    }

    internal fun cacheVocabularyIfNeeded(projectDir: File, vocabulary: OcrVocabulary) {
        if (projectDir.path == ".") {
            cachedDefaultVocabulary = vocabulary
        } else {
            LogManager.d(TAG, "Repertoire personnalise '${projectDir.path}', non mis en cache")
        }
    }

    /**
     * Resets the internal vocabulary cache (used for unit testing).
     */
    internal fun clearCache() {
        cachedDefaultVocabulary = null
    }

    private fun findResValuesDir(startDir: File): File? {
        val candidates = listOf(
            File(startDir, "src/main/res/values"),
            File(startDir, "app/src/main/res/values"),
            File(startDir, "../app/src/main/res/values"),
            File(startDir, "../../app/src/main/res/values"),
        )
        return candidates.firstOrNull { it.isDirectory }
    }

    internal fun parseResDirectory(
        resDir: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>
    ) {
        val xmlFiles = resDir.listFiles { _, name -> name.endsWith(".xml") }
        if (xmlFiles != null) {
            for (xmlFile in xmlFiles) {
                parseXmlResourceFile(xmlFile, stringArrays, strings)
            }
        } else {
            LogManager.w(TAG, "No XML files found in directory: ${resDir.absolutePath}")
        }
    }

    internal fun parseXmlResourceFile(
        file: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>,
    ) {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            parseXmlDocument(doc, stringArrays, strings)
        } catch (e: Exception) {
            LogManager.e(TAG, "Error parsing XML resource file ${file.name}: ${e.message}", e)
        }
    }

    internal fun parseXmlDocument(
        doc: Document,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>,
    ) {
        doc.documentElement.normalize()

        val arrayNodes = doc.getElementsByTagName("string-array")
        for (i in 0 until arrayNodes.length) {
            val node = arrayNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val elem = node as Element
                val arrayName = elem.getAttribute("name")
                val items = mutableListOf<String>()
                val itemNodes = elem.getElementsByTagName("item")
                for (j in 0 until itemNodes.length) {
                    items.add(itemNodes.item(j).textContent.trim().replace("\\'", "'"))
                }
                stringArrays[arrayName] = items
            } else {
                LogManager.w(TAG, "Skipping non-element string-array node")
            }
        }

        val stringNodes = doc.getElementsByTagName("string")
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val elem = node as Element
                val stringName = elem.getAttribute("name")
                val rawText = elem.textContent.trim()
                strings[stringName] = rawText.replace("\\\\", "\\")
            } else {
                LogManager.w(TAG, "Skipping non-element string node")
            }
        }
    }

    internal fun getList(map: Map<String, List<String>>, key: String): List<String> {
        return map[key] ?: emptyList()
    }

    internal fun getStringVal(map: Map<String, String>, key: String): String {
        return map[key] ?: ""
    }
}
