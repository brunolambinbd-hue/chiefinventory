package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Utility to parse and identify source-related information (hotels, addresses, phones) from OCR text.
 */
object SourceParser {
    private val zipCodeRegex = Regex("\\b\\d{4,5}\\b")
    
    // Regex améliorée pour capturer les formats comme 02/542.42.42 ou +32 2 542 42 42
    private val phoneRegex = Regex("(?:(?:\\+|00)\\d{1,3}[\\s./-]?)?(?:\\(0\\)[\\s./-]?)?\\d(?:[\\s./-]*\\d{2,3}){2,5}")

    /**
     * Data class to hold source resources loaded from XML.
     */
    data class SourceResources(
        val sourceKeywords: List<String>,
        val phonePrefixes: List<String>
    )

    /**
     * Loads source-related strings from application resources.
     */
    fun loadResources(res: Resources): SourceResources {
        return SourceResources(
            sourceKeywords = res.getStringArray(R.array.source_keywords).toList(),
            phonePrefixes = res.getStringArray(R.array.phone_prefixes).toList()
        )
    }

    /**
     * Checks if a given line likely contains source info (address, phone, hotel name).
     */
    fun isSourceLine(line: String, resources: SourceResources): Boolean {
        val lowerLine = line.lowercase()
        return phoneRegex.containsMatchIn(line) || 
               resources.sourceKeywords.any { lowerLine.contains(it) } || 
               (zipCodeRegex.containsMatchIn(line) && line.length < 50)
    }

    /**
     * Cleans the source line by removing common phone prefixes.
     * Uses word boundaries to avoid damaging words like "Hôtel".
     */
    fun cleanSourceLine(line: String, resources: SourceResources): String {
        var cleaned = line
        resources.phonePrefixes.forEach { prefix ->
            // On ajoute \b devant le préfixe pour s'assurer que c'est un mot indépendant
            // On gère aussi le point éventuel après le préfixe (ex: Tél.)
            val pattern = "(?i)\\b$prefix\\b\\s*[:\\-.]?"
            cleaned = cleaned.replace(Regex(pattern, RegexOption.IGNORE_CASE), "").trim()
        }
        return cleaned
    }
}
