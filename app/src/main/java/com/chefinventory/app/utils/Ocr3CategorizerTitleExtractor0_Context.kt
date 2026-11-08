package com.chefinventory.app.utils

/**
 * Sous-composant du Bloc 0 de l'Étape 3 : Extraie les mots-clés d'ingrédients et recherche le premier ingrédient de la recette pour [Ocr3CategorizerTitleExtractor].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerTitleExtractor0_Context {
    private const val MIN_SIGNIFICANT_WORD_LENGTH = 3

    /**
     * Extraie les mots des noms d'ingrédients présents sur la fiche scannée de la recette.
     *
     * @param lines La liste des lignes brutes de la recette.
     * @param vocabulary Le vocabulaire métier data-driven.
     * @return L'ensemble des mots significatifs d'ingrédients de longueur >= 3.
     */
    internal fun extractRecipeIngredientWords(lines: List<String>, vocabulary: OcrVocabulary? = null): Set<String> {
        val ingredientNameWords = mutableSetOf<String>()
        val activeVocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val ignoreWords = (activeVocab.culinaryMeasurementUnits + activeVocab.standaloneArticles).map { it.lowercase().trim() }.toSet()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val startsDigit = OcrRegexes.Titles.STARTS_WITH_DIGIT.containsMatchIn(trimmed)
            val hasUnitWord = OcrRegexes.Titles.INGREDIENT_UNIT_WORD.containsMatchIn(trimmed)
            if (startsDigit || hasUnitWord) {
                val parsed = IngredientParser.parse(trimmed, activeVocab.culinaryMeasurementUnits)
                if (parsed.name.isNotBlank()) {
                    val words = parsed.name.split(OcrRegexes.Titles.PUNCTUATION_SPLIT)
                        .map { it.lowercase().trim() }
                        .filter { it.length >= MIN_SIGNIFICANT_WORD_LENGTH && it !in ignoreWords }
                    ingredientNameWords.addAll(words)
                    words.forEach { w ->
                        if (w.endsWith("s") || w.endsWith("x")) {
                            ingredientNameWords.add(w.dropLast(1))
                        }
                    }
                }
            }
        }
        return ingredientNameWords
    }

    /**
     * Extraie le premier mot significatif du nom nettoyé d'un ingrédient en excluant les mots vides.
     */
    internal fun extractFirstIngredientNameWord(parsedName: String, stopWords: List<String>): String? {
        val trimmedName = parsedName.trim()
        val isBlankName = trimmedName.isBlank()
        if (isBlankName) {
            return null
        }

        val words = trimmedName.split(OcrRegexes.Titles.PUNCTUATION_SPLIT)
            .map { it.lowercase().trim() }
            .filter { it.length >= MIN_SIGNIFICANT_WORD_LENGTH && it !in stopWords }

        val hasWords = words.isNotEmpty()
        if (hasWords) {
            return words.first()
        }
        return null
    }

    /**
     * Recherche le nom du premier ingrédient de la recette pour le départage des scores.
     *
     * @param lines La liste des lignes brutes de la recette.
     * @param vocabulary Le vocabulaire métier data-driven (`ocr_step25_title_repairs.xml`).
     * @return Le premier mot d'ingrédient significatif trouvé, ou null.
     */
    internal fun findFirstIngredientWord(lines: List<String>, vocabulary: OcrVocabulary? = null): String? {
        val activeVocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val stopWords = activeVocab.standaloneArticles.map { it.trim().lowercase() }

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val startsDigit = OcrRegexes.Titles.STARTS_WITH_DIGIT.containsMatchIn(trimmed)
            val hasUnitWord = OcrRegexes.Titles.INGREDIENT_UNIT_WORD.containsMatchIn(trimmed)
            if (startsDigit || hasUnitWord) {
                val parsed = IngredientParser.parse(trimmed, activeVocab.culinaryMeasurementUnits)
                val word = extractFirstIngredientNameWord(parsed.name, stopWords)
                if (word != null) {
                    return word
                }
            }
        }
        return null
    }
}
