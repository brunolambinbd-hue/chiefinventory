package com.example.parabdcollector.utils

import java.util.regex.Pattern

/**
 * Un objet utilitaire pour extraire des informations structurées 
 * d'une chaîne de description ou d'un titre.
 */
object DescriptionParser {

    // Pattern pour le tirage: cherche un nombre suivi de "ex" (ex: "350 ex"), insensible à la casse.
    private val tiragePattern = Pattern.compile("(\\d+)\\s*ex", Pattern.CASE_INSENSITIVE)

    // Liste des patterns pour les dimensions.
    private val dimensionPatterns = listOf(
        Pattern.compile("(\\d+\\s*/\\s*\\d+)"), // Cherche "xx/yy" ou "xx / yy"
        Pattern.compile("(A\\d+)", Pattern.CASE_INSENSITIVE)      // Cherche "A4", "A5", etc., insensible à la casse.
    )

    data class ParsedInfo(
        val tirage: String?,
        val dimensions: String?
    )

    fun parse(titre: String?, description: String?): ParsedInfo {
        var tirage: String? = null
        var dimensions: String? = null

        // --- Recherche du TIRAGE ---
        // Étape 1: Chercher dans le titre.
        if (titre != null) {
            val tirageMatcher = tiragePattern.matcher(titre)
            if (tirageMatcher.find()) {
                tirage = tirageMatcher.group(1)
            }
        }
        // Étape 2: Si rien n'est trouvé, chercher dans la description.
        if (tirage == null && description != null) {
            val tirageMatcher = tiragePattern.matcher(description)
            if (tirageMatcher.find()) {
                tirage = tirageMatcher.group(1)
            }
        }

        // --- Recherche des DIMENSIONS ---
        // Étape 1: Chercher dans le titre.
        if (titre != null) {
            for (pattern in dimensionPatterns) {
                val matcher = pattern.matcher(titre)
                if (matcher.find()) {
                    dimensions = matcher.group(1)
                    break // On a trouvé, on arrête.
                }
            }
        }
        // Étape 2: Si rien n'est trouvé, chercher dans la description.
        if (dimensions == null && description != null) {
            for (pattern in dimensionPatterns) {
                val matcher = pattern.matcher(description)
                if (matcher.find()) {
                    dimensions = matcher.group(1)
                    break // On a trouvé, on arrête.
                }
            }
        }

        return ParsedInfo(tirage, dimensions)
    }
}