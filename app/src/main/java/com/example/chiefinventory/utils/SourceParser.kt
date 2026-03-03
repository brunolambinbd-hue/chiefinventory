package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Utility to parse and identify source-related information (hotels, addresses, phones) from OCR text.
 */
object SourceParser {
    private val zipCodeRegex = Regex("\\b\\d{4,5}\\b")
    private val phoneRegex = Regex("(?:(?:\\+|00)32|0)[\\s./-]*[1-9](?:[\\s./-]*\\d{2}){3,4}")

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
               zipCodeRegex.containsMatchIn(line)
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
