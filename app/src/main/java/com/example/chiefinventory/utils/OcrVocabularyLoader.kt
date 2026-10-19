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
object OcrVocabularyLoader {

    /**
     * Finds and parses all XML resource files in `src/main/res/values/` starting from [projectDir].
     * @return An [OcrVocabulary] populated with the actual production resource data.
     */
    fun loadFromProjectResources(projectDir: File = File(".")): OcrVocabulary {
        val resDir = findResValuesDir(projectDir)
            ?: throw IllegalStateException("Could not locate 'res/values' directory from: ${projectDir.absolutePath}")

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        resDir.listFiles { _, name -> name.endsWith(".xml") }?.forEach { xmlFile ->
            parseXmlResourceFile(xmlFile, stringArrays, strings)
        }

        return OcrVocabulary(
            stepActionKeywords = stringArrays["step_action_keywords"] ?: emptyList(),
            commonIngredientsNoQty = stringArrays["common_ingredients_no_qty"] ?: emptyList(),
            excludedOcrKeywords = stringArrays["excluded_ocr_keywords"] ?: emptyList(),
            ingredientPreparationModifiers = stringArrays["ingredient_preparation_modifiers"] ?: emptyList(),
            instructionSwitchKeywords = stringArrays["instruction_switch_keywords"] ?: emptyList(),
            ingredientSemanticExclusions = stringArrays["ingredient_semantic_exclusions"] ?: emptyList(),
            ingredientEndMarkers = stringArrays["ingredient_end_markers"] ?: emptyList(),
            ingredientSubHeaders = stringArrays["ingredient_sub_headers"] ?: emptyList(),
            sourceKeywords = stringArrays["source_keywords"] ?: emptyList(),
            phonePrefixes = stringArrays["phone_prefixes"] ?: emptyList(),
            wineAppellations = stringArrays["wine_appellations"] ?: emptyList(),
            wineProducers = stringArrays["wine_producers"] ?: emptyList(),
            wineKeywords = stringArrays["wine_keywords"] ?: emptyList(),
            wineTitleKeywords = stringArrays["wine_title_keywords"] ?: emptyList(),
            wineRemovePattern = strings["wine_remove_pattern"] ?: "",
            ocrSpellingRepairs = stringArrays["ocr_spelling_repairs"] ?: emptyList(),
            adExclusions = stringArrays["advertisement_exclusions"] ?: emptyList(),
            titleKeywords = stringArrays["title_keywords"] ?: emptyList(),
            mandatoryComplementIngredients = stringArrays["ingredient_mandatory_complements"] ?: emptyList()
        )
    }

    private fun findResValuesDir(startDir: File): File? {
        val candidates = listOf(
            File(startDir, "src/main/res/values"),
            File(startDir, "app/src/main/res/values"),
            File(startDir, "../app/src/main/res/values"),
            File(startDir, "../../app/src/main/res/values")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
    }

    private fun parseXmlResourceFile(
        file: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>
    ) {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            doc.documentElement.normalize()

            // Parse <string-array name="...">
            val arrayNodes = doc.getElementsByTagName("string-array")
            for (i in 0 until arrayNodes.length) {
                val node = arrayNodes.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val elem = node as Element
                    val arrayName = elem.getAttribute("name")
                    val items = mutableListOf<String>()
                    val itemNodes = elem.getElementsByTagName("item")
                    for (j in 0 until itemNodes.length) {
                        items.add(itemNodes.item(j).textContent.trim())
                    }
                    stringArrays[arrayName] = items
                }
            }

            // Parse <string name="...">
            val stringNodes = doc.getElementsByTagName("string")
            for (i in 0 until stringNodes.length) {
                val node = stringNodes.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val elem = node as Element
                    val stringName = elem.getAttribute("name")
                    val rawText = elem.textContent.trim()
                    // Dé-échappement des doubles antislashs Android XML (ex: \\s+ -> \s+)
                    strings[stringName] = rawText.replace("\\\\", "\\")
                }
            }
        } catch (e: Exception) {
            System.err.println("Error parsing XML resource file ${file.name}: ${e.message}")
        }
    }
}
