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
        // Pattern amélioré: cherche "xx/yy" ou "xx,x/yy,y", avec virgule ou point comme séparateur.
        Pattern.compile("(\\d+([.,]\\d+)?\\s*/\\s*\\d+([.,]\\d+)?)"), 
        Pattern.compile("(A\\d+)", Pattern.CASE_INSENSITIVE)      // Cherche "A4", "A5", etc., insensible à la casse.
    )

    /**
     * Contient les informations extraites par le [DescriptionParser].
     * @property tirage Le tirage extrait (ex: "350").
     * @property dimensions Les dimensions extraites (ex: "25x35cm").
     */
    data class ParsedInfo(
        val tirage: String?,
        val dimensions: String?
    )

    /**
     * Extrait les informations de tirage et de dimensions à partir d'un titre et d'une description.
     *
     * La fonction recherche les motifs définis dans les deux chaînes combinées.
     *
     * @param titre Le titre de l'objet, qui peut contenir des informations.
     * @param description La description de l'objet.
     * @return Un objet [ParsedInfo] contenant les informations trouvées.
     */
    fun parse(titre: String?, description: String?): ParsedInfo {
        var tirage: String? = null
        var dimensions: String? = null

        val combinedString = listOfNotNull(titre, description).joinToString(separator = " ")

        // --- Recherche du TIRAGE ---
        val tirageMatcher = tiragePattern.matcher(combinedString)
        if (tirageMatcher.find()) {
            tirage = tirageMatcher.group(1)
        }

        // --- Recherche des DIMENSIONS ---
        for (pattern in dimensionPatterns) {
            val matcher = pattern.matcher(combinedString)
            if (matcher.find()) {
                dimensions = matcher.group(1)
                break // On a trouvé, on arrête.
            }        }

        return ParsedInfo(tirage, dimensions)
    }
}
