package com.example.parabdcollector.utils

import java.util.regex.Pattern

/**
 * Un objet utilitaire pour extraire des informations structurées 
 * d'une chaîne de description.
 */
object DescriptionParser {

    // Pattern pour le tirage: cherche un nombre suivi de "ex" (ex: "350 ex")
    private val tiragePattern = Pattern.compile("(\\d+)\\s*ex")

    // Pattern pour les dimensions: cherche un format comme "25/30"
    private val dimensionsPattern = Pattern.compile("(\\d+/\\d+)")

    data class ParsedInfo(
        val tirage: String?,
        val dimensions: String?
    )

    fun parse(description: String): ParsedInfo {
        var tirage: String? = null
        var dimensions: String? = null

        val tirageMatcher = tiragePattern.matcher(description)
        if (tirageMatcher.find()) {
            tirage = tirageMatcher.group(1)
        }

        val dimensionsMatcher = dimensionsPattern.matcher(description)
        if (dimensionsMatcher.find()) {
            dimensions = dimensionsMatcher.group(1)
        }

        return ParsedInfo(tirage, dimensions)
    }
}