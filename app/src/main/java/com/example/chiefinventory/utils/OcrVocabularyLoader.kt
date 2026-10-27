package com.example.chiefinventory.utils

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Utility to load production [OcrVocabulary] directly from the Android XML resource files (in `res/values/`).
 * This enables local JVM unit tests to run as true non-regression tests using the real production data files
 * without requiring an Android emulator or Mockito mocks.
 */
@Suppress("IfThenToElvis", "FoldInitializerAndIfNull", "kotlin:S6529")
object OcrVocabularyLoader {

    private fun getList(map: Map<String, List<String>>, key: String): List<String> {
        val list = map[key]
        return if (list != null) list else emptyList()
    }

    private fun getStringVal(map: Map<String, String>, key: String): String {
        val str = map[key]
        return if (str != null) str else ""
    }

    /**
     * Finds and parses all XML resource files in `src/main/res/values/` starting from [projectDir].
     * @return An [OcrVocabulary] populated with the actual production resource data.
     * @throws IllegalStateException If the 'res/values' directory cannot be located.
     */
    fun loadFromProjectResources(projectDir: File = File(".")): OcrVocabulary {
        val resDir = findResValuesDir(projectDir)
        if (resDir == null) {
            val errorMsg = "Could not locate 'res/values' directory from: ${projectDir.absolutePath}"
            com.parabd.lib.traceability.utils.LogManager.e("OcrVocabularyLoader", errorMsg)
            throw IllegalStateException(errorMsg)
        }

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        val xmlFiles = resDir.listFiles { _, name -> name.endsWith(".xml") }
        if (xmlFiles != null) {
            for (xmlFile in xmlFiles) {
                parseXmlResourceFile(xmlFile, stringArrays, strings)
            }
        }

        return OcrVocabulary(
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
            ingredientConnectors = getList(stringArrays, "ingredient_connectors")
        )
    }

    private fun findResValuesDir(startDir: File): File? {
        val candidates = listOf(
            File(startDir, "src/main/res/values"),
            File(startDir, "app/src/main/res/values"),
            File(startDir, "../app/src/main/res/values"),
            File(startDir, "../../app/src/main/res/values")
        )
        for (dir in candidates) {
            if (dir.exists()) {
                if (dir.isDirectory) {
                    return dir
                }
            }
        }
        return null
    }

    internal fun parseXmlResourceFile(
        file: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>
    ) {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
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
                }
            }
        } catch (e: Exception) {
            com.parabd.lib.traceability.utils.LogManager.e("OcrVocabularyLoader", "Error parsing XML resource file ${file.name}: ${e.message}", e)
        }
    }
}
