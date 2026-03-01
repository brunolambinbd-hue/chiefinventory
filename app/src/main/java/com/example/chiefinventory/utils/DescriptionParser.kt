package com.example.chiefinventory.utils

import java.util.regex.Pattern

/**
 * A utility object for parsing structured information like print run and dimensions
 * from unstructured title and description strings.
 */
object DescriptionParser {

    // Pattern for the print run: looks for a number followed by "ex" (e.g., "350 ex"), case-insensitive.
    private val tiragePattern = Pattern.compile("(\\d+)\\s*ex", Pattern.CASE_INSENSITIVE)

    // List of patterns for dimensions.
    private val dimensionPatterns = listOf(
        // Improved pattern: looks for "xx/yy" or "xx.x/yy.y", with comma or dot as a separator.
        Pattern.compile("(\\d+([.,]\\d+)?\\s*/\\s*\\d+([.,]\\d+)?)"),
        Pattern.compile("(A\\d+)", Pattern.CASE_INSENSITIVE)      // Looks for "A4", "A5", etc., case-insensitive.
    )

    /**
     * Holds the information extracted by the [DescriptionParser].
     * @property tirage The extracted print run (e.g., "350").
     * @property dimensions The extracted dimensions (e.g., "25x35cm").
     */
    data class ParsedInfo(
        val tirage: String?,
        val dimensions: String?
    )

    /**
     * Extracts print run and dimensions information from a title and a description.
     *
     * The function searches for the defined patterns in both strings combined.
     *
     * @param titre The title of the item, which may contain information.
     * @param description The description of the item.
     * @return A [ParsedInfo] object containing the found information.
     */
    fun parse(titre: String?, description: String?): ParsedInfo {
        var tirage: String? = null
        var dimensions: String? = null

        val combinedString = listOfNotNull(titre, description).joinToString(separator = " ")

        // --- Search for PRINT RUN ---
        val tirageMatcher = tiragePattern.matcher(combinedString)
        if (tirageMatcher.find()) {
            tirage = tirageMatcher.group(1)
        }

        // --- Search for DIMENSIONS ---
        for (pattern in dimensionPatterns) {
            val matcher = pattern.matcher(combinedString)
            if (matcher.find()) {
                dimensions = matcher.group(1)
                break // Found, we stop.
            }
        }

        return ParsedInfo(tirage, dimensions)
    }
}
