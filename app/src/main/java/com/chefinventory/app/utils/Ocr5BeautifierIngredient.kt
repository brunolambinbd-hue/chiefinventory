package com.chefinventory.app.utils

/**
 * Assistant dédié à la beautification et au nettoyage final des lignes d'ingrédients lors de l'Étape 5.
 *
 * Responsabilités :
 * - Regrouper les qualificatifs de variétés d'ingrédients orphelines réparties sur 2 lignes ([Ocr5BeautifierIngredientVarietyMerger]).
 * - Forcer la majuscule initiale sur chaque nom d'ingrédient.
 * - Supprimer les en-têtes de section résiduels ("INGRÉDIENTS:", "Composition:").
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5BeautifierIngredient {

    /**
     * Effectue la beautification et le regroupement des qualificatifs de variétés d'ingrédients orphelins.
     *
     * @param ingredients La liste des lignes d'ingrédients issues de [Ocr4Merger].
     * @param baseIngredients Liste des ingrédients de base durant un qualificatif (`ingredient_varieties.xml`).
     * @param varietyQualifiers Liste des qualificatifs de variétés (`ingredient_varieties.xml`).
     * @return La liste nettoyée des ingrédients avec majuscule initiale et sans en-têtes résiduels.
     */
    fun beautifyIngredients(
        ingredients: List<String>,
        baseIngredients: List<String> = emptyList(),
        varietyQualifiers: List<String> = emptyList()
    ): List<String> {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val standaloneTrashWords = (vocab.standaloneArticles + vocab.hangingConnectors + vocab.priceTitleKeywords + vocab.wineTitleKeywords)
            .map { it.lowercase().trim() }
            .toSet()
        val headerTrashWords = vocab.ocr1ProtectedIngredientsHeaders.map { it.lowercase().trim() }.toSet()

        val mergedVarieties = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(ingredients, baseIngredients, varietyQualifiers)
        return mergedVarieties.map { line ->
            var cleaned = line.trim()
            if (cleaned.isNotEmpty()) {
                val first = cleaned[0]
                if (first.isLowerCase()) {
                    cleaned = first.titlecase() + cleaned.substring(1)
                } else {
                    // Le premier caractère est déjà une majuscule ou un chiffre (ex: "250 g de farine")
                }
            }
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(":").removeSuffix(".")
            val isHeaderTrash = headerTrashWords.any { header -> lower == header || lower.startsWith("$header ") || lower.startsWith("$header(") || lower.startsWith("$header:") }
            line.isNotBlank() && !isHeaderTrash && lower !in standaloneTrashWords
        }
    }
}
