package com.example.chiefinventory.utils

/**
 * Assistant dédié à la beautification et au nettoyage final des lignes d'ingrédients lors de l'Étape 5.
 *
 * Responsabilités :
 * - Regrouper les qualificatifs de variétés d'ingrédients orphelines réparties sur 2 lignes ([IngredientVarietyMerger]).
 * - Forcer la majuscule initiale sur chaque nom d'ingrédient.
 * - Supprimer les en-têtes de section résiduels ("INGRÉDIENTS:", "Composition:").
 */
object Ocr5IngredientBeautifier {

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
        val mergedVarieties = IngredientVarietyMerger.beautifyIngredientVarieties(ingredients, baseIngredients, varietyQualifiers)
        return mergedVarieties.map { line ->
            var cleaned = line.trim()
            if (cleaned.isNotEmpty()) {
                cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(":")
            line.isNotBlank() && lower != "ingrédients" && lower != "ingredients" && lower != "composition" && lower != "ngrédients"
        }
    }
}
