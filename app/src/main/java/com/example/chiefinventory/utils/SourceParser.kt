package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Utility to parse and identify source-related information (hotels, addresses, phones) from OCR text.
 */
object SourceParser {
    private val zipCodeRegex = Regex("\\b\\d{4,5}\\b")
    
    // Regex améliorée pour capturer les formats comme 02/542.42.42 ou +32 2 542 42 42
    // Elle cherche un début de numéro suivi de groupes de 2 ou 3 chiffres séparés par des symboles courants
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
        // On considère que c'est une source si :
        // - Contient un numéro de téléphone (priorité haute)
        // - Contient un mot clé de source (rue, hôtel, etc.)
        // - Contient un code postal ET est assez court
        return phoneRegex.containsMatchIn(line) || 
               resources.sourceKeywords.any { lowerLine.contains(it) } || 
               (zipCodeRegex.containsMatchIn(line) && line.length < 50)
    }

    /**
     * Cleans the source line by removing common phone prefixes.
     */
    fun cleanSourceLine(line: String, resources: SourceResources): String {
        var cleaned = line
        resources.phonePrefixes.forEach { prefix ->
            cleaned = cleaned.replace(Regex("(?i)$prefix\\s*[:\\-.]?", RegexOption.IGNORE_CASE), "").trim()
        }
        return cleaned
    }
}
