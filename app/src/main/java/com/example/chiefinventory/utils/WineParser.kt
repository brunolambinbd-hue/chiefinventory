package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Utility to parse and identify wine-related information from OCR text.
 */
object WineParser {
    private val wineYearRegex = Regex("\\b(?:19|20)\\d{2}\\b")
    private val wineVolRegex = Regex("\\b\\d+(?:[\\s.,]\\d+)?\\s*(?:cl|ml|l|vol)\\b", RegexOption.IGNORE_CASE)
    private val extraWineCleanRegex = Regex("(?i)^(?:accord|boisson|boire|servir avec|suggestion)\\s*:?", RegexOption.IGNORE_CASE)

    data class WineResources(
        val appellations: List<String>,
        val producers: List<String>,
        val keywords: List<String>,
        val titleKeywords: List<String>,
        val removePattern: String
    )

    fun loadResources(res: Resources): WineResources {
        return WineResources(
            appellations = res.getStringArray(R.array.wine_appellations).toList(),
            producers = res.getStringArray(R.array.wine_producers).toList(),
            keywords = res.getStringArray(R.array.wine_keywords).toList(),
            titleKeywords = res.getStringArray(R.array.wine_title_keywords).toList(),
            removePattern = res.getString(R.string.wine_remove_pattern)
        )

    }

    fun isWineLine(line: String, resources: WineResources): Boolean {
        val lowerLine = line.lowercase()
        if (lowerLine.contains("vinaigre")) return false

        // Rule: Year + Volume is a very strong indicator of a wine bottle
        if (wineYearRegex.containsMatchIn(line) && wineVolRegex.containsMatchIn(line)) return true

        // Helper to check for whole word matches using Unicode-aware lookarounds
        fun containsWholeWord(text: String, keyword: String): Boolean {
            val pattern = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(keyword)}(?![\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(text)
        }

        return resources.appellations.any { containsWholeWord(line, it) } ||
                resources.producers.any { containsWholeWord(line, it) } ||
                resources.keywords.any { containsWholeWord(line, it) } ||
                resources.titleKeywords.any { containsWholeWord(line, it) }
    }

    fun cleanWineLine(line: String, resources: WineResources): String {
        val wineRemoveRegex = Regex(resources.removePattern, RegexOption.IGNORE_CASE)
        var cleaned = line.replace(wineRemoveRegex, "")
        cleaned = cleaned.replace(extraWineCleanRegex, "").trim()
        cleaned = cleaned.replace(Regex("^[:\\-\\s\\.]+"), "").trim()

        return applySpellingCorrections(cleaned)
    }

    /**
     * Applies common spelling corrections for wine names misread by OCR.
     */
    private fun applySpellingCorrections(text: String): String {
        var corrected = text

        // Correct "Bordeau" to "Bordeaux" (case-insensitive, word boundary)
        corrected = corrected.replace(Regex("(?i)\\bBordeau\\b"), "Bordeaux")

        // Correct "Atinum" to "Atinium" if necessary (based on previous logs)
        corrected = corrected.replace(Regex("(?i)\\bAtinum\\b"), "Atinium")
        corrected = corrected.replace(Regex("(?i)\\bChậteau\\b"), "Château")


        return corrected
    }
}
