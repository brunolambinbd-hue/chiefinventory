package com.chefinventory.app.utils

import java.text.Normalizer

/**
 * Sous-composant du Bloc B de l'Étape 2.5 : Extraction des catégories de prix et budget pour [RecipeMetadataParser].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object RecipeMetadataParser2_BlockB_Price {

    /**
     * Extraie et qualifie la catégorie de prix/budget de la ligne [rawWorkingLine].
     *
     * @return La ligne nettoyée du prix.
     */
    internal fun processPriceBlock(rawWorkingLine: String, vocabulary: OcrVocabulary?, results: RawSections): String {
        var workingLine = rawWorkingLine

        val hasLowCat = if (vocabulary != null) vocabulary.priceCategoriesLow.isNotEmpty() else false
        val priceCategoriesLow = if (hasLowCat) {
            vocabulary!!.priceCategoriesLow
        } else {
            listOf("très bon marché", "tres bon marche", "bon marché", "bon marche", "économique", "economique", "petit prix", "pas cher")
        }

        val hasMedCat = if (vocabulary != null) vocabulary.priceCategoriesMedium.isNotEmpty() else false
        val priceCategoriesMedium = if (hasMedCat) {
            vocabulary!!.priceCategoriesMedium
        } else {
            listOf("coût moyen", "cout moyen", "prix moyen", "budget moyen", "moyen", "moyennement cher")
        }

        val hasHighCat = if (vocabulary != null) vocabulary.priceCategoriesHigh.isNotEmpty() else false
        val priceCategoriesHigh = if (hasHighCat) {
            vocabulary!!.priceCategoriesHigh
        } else {
            listOf("très cher", "tres cher", "assez cher", "cher", "élevé", "eleve", "gastronomique", "coûteux", "couteux")
        }

        val hasTitleKw = if (vocabulary != null) vocabulary.priceTitleKeywords.isNotEmpty() else false
        val priceTitles = if (hasTitleKw) {
            vocabulary!!.priceTitleKeywords
        } else {
            listOf("prix :", "prix", "coût :", "cout :", "coût", "cout", "budget :", "budget")
        }

        val normLine = Normalizer.normalize(workingLine.lowercase(), Normalizer.Form.NFC)

        val matchedLow = priceCategoriesLow.firstOrNull { cat ->
            val normCat = Normalizer.normalize(cat.lowercase(), Normalizer.Form.NFC)
            OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find()
        }

        val matchedMedium = if (matchedLow == null) {
            priceCategoriesMedium.firstOrNull { cat ->
                val normCat = Normalizer.normalize(cat.lowercase(), Normalizer.Form.NFC)
                OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find()
            }
        } else {
            null
        }

        val matchedHigh = if (matchedLow == null && matchedMedium == null) {
            priceCategoriesHigh.firstOrNull { cat ->
                val normCat = Normalizer.normalize(cat.lowercase(), Normalizer.Form.NFC)
                OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find()
            }
        } else {
            null
        }

        val matchedCategory = matchedLow ?: matchedMedium ?: matchedHigh

        if (matchedCategory != null) {
            if (results.detectedPriceRating == null) {
                val formattedPrice = when {
                    matchedLow != null -> "Bon marché"
                    matchedMedium != null -> "Coût moyen"
                    else -> "Assez cher"
                }
                results.detectedPriceRating = formattedPrice
            }
            workingLine = OcrRegexes.getSpellingRepairPattern(matchedCategory).matcher(workingLine).replaceAll("").trim()
            priceTitles.forEach { pTitle ->
                workingLine = OcrRegexes.getSpellingRepairPattern(pTitle).matcher(workingLine).replaceAll("").trim()
            }
        }

        return workingLine
    }
}
