package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 2 (Nettoyage) dédié à l'inspection des lignes publicitaires/marketing et à l'extraction des marques/enseignes.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr2CleanerAdSourceExtractor {

    /**
     * Inspecte une ligne publicitaire ou marketing et en extrait la marque ou l'enseigne culinaire connue ([OcrVocabulary.knownSourceBrands]).
     *
     * @param line La ligne publicitaire à analyser (ex: "Assiette de présentation disponible dans les Maxi GB").
     * @param vocabulary Le vocabulaire métier OCR contenant la liste blanche des enseignes.
     * @return Le nom exact de l'enseigne/marque trouvée (ex: "Maxi GB"), ou `null` si aucune marque de la liste n'est présente.
     */
    fun extractSourceFromAdLine(line: String, vocabulary: OcrVocabulary): String? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || vocabulary.knownSourceBrands.isEmpty()) return null
        return vocabulary.knownSourceBrands.firstOrNull { brand ->
            val cleanBrand = brand.trim()
            cleanBrand.isNotEmpty() && Regex("(?i)(?<![\\p{L}\\p{N}])${Regex.escape(cleanBrand)}(?![\\p{L}\\p{N}])").containsMatchIn(trimmed)
        }
    }
}
