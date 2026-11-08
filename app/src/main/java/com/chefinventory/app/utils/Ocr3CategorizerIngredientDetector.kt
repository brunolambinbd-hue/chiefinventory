package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 3 (Catégorisation) dédié au traitement et à la détection des ingrédients.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerIngredientDetector {
    private const val MIN_CLEANED_INGREDIENT_LENGTH = 1

    /**
     * Détermine si une ligne contient un poids ou un volume d'ingrédient explicite (ex: 200 g, 1 kg, 5 cl, 250 ml).
     * Exclut automatiquement les volumes de bouteilles de vin (`WineParser.hasWineVolume`).
     *
     * @param line La ligne de texte à analyser.
     * @return `true` si un poids ou volume explicite est trouvé, `false` sinon.
     */
    fun hasExplicitWeight(line: String): Boolean {
        return Regex("(?i)\\b\\d+(?:[.,]\\d+)?\\s*(?:g|kg|mg|cl|ml|dl|l|g\\.|kg\\.|cl\\.|ml\\.|dl\\.|l\\.)\\b").containsMatchIn(line)
    }

    /**
     * Compte le nombre de séquences d'ingrédients distinctes présentes sur une seule ligne.
     * Ignore les quantités situées à l'intérieur de parenthèses ou suivies d'unités de temps/dimensions.
     *
     * @param line La ligne de texte à analyser.
     * @return Le nombre de séquences d'ingrédients dénombrées.
     */
    fun countIngredientSequences(line: String): Int {
        if (line.isBlank()) return 0
        val qtyPattern = "(?:\\d+(?:/\\d+)?|(?<=\\s|^)[1Il!|](?!\\p{L})(?:/\\d+)?)"
        val pattern = Regex("(?:^|(?<=\\s))$qtyPattern", RegexOption.IGNORE_CASE)
        
        val excludedNextWords = setOf("mm", "cm", "min", "sec", "ou", "et", "à", "sur")
        
        return pattern.findAll(line).count { match ->
            if (OcrTextUtils.isInsideParentheses(line, match.range.first)) return@count false
            
            val textAfter = line.substring(match.range.last + 1).trimStart()
            val nextWord = textAfter.split(Regex("[\\s,.;]")).firstOrNull()?.lowercase()
            
            nextWord != null && nextWord !in excludedNextWords && nextWord.any { it.isLetter() }
        }
    }

    /**
     * Effectue le nettoyage sémantique d'une ligne d'ingrédient (retrait des puces, validation de longueur, filtrage des articles isolés).
     *
     * @param text La ligne d'ingrédient brute.
     * @param excludedKeywords Liste des mots-clés d'exclusion OCR (`excluded_keywords.xml`).
     * @param semanticExclusions Liste des exclusions sémantiques d'ingrédients (`ingredient_exclusion_arrays.xml`).
     * @param standaloneArticles Liste des articles isolés à vider (`standalone_articles.xml`).
     * @return La ligne d'ingrédient nettoyée ou une chaîne vide si la ligne doit être supprimée.
     */
    fun cleanIngredientSemantics(
        text: String, 
        excludedKeywords: List<String>, 
        semanticExclusions: List<String> = emptyList(),
        standaloneArticles: List<String> = emptyList()
    ): String {
        val cleaned = text.trim().replace(Regex("^[•\\-*]\\s*"), "")
        if (!cleaned.any { it.isLetter() } || cleaned.length <= MIN_CLEANED_INGREDIENT_LENGTH) return ""
        if (Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource(cleaned)) return ""
        
        if (OcrTextUtils.isExcluded(cleaned, excludedKeywords)) return ""
        if (OcrTextUtils.isExcluded(cleaned, semanticExclusions)) return ""

        val activeArticles = if (standaloneArticles.isNotEmpty()) {
            standaloneArticles
        } else {
            OcrVocabularyLoader.loadFromProjectResources().standaloneArticles
        }

        val words = cleaned.split(Regex("\\s+"))
        if (words.size == 1) {
            val cleanWord = words[0].lowercase().replace(Regex("[^\\p{L}]"), "")
            if (activeArticles.any { it.equals(cleanWord, ignoreCase = true) }) {
                return ""
            }
        }

        return cleaned
    }
}
