package com.example.chiefinventory.utils

/**
 * Étape 5 du pipeline OCR : Façade Orchestratrice de Beautification et Présentation Finale à l'Écran.
 *
 * Responsabilités :
 * - Déléguer la beautification des consignes d'instructions à [Ocr5InstructionBeautifier].
 * - Déléguer la beautification des ingrédients à [Ocr5IngredientBeautifier].
 */
object Ocr5Beautifier {

    /**
     * Effectue la beautification et le recollage sémantique final des consignes d'instructions.
     *
     * @param instructions La liste des lignes d'instructions fusionnées issues de [Ocr4Merger].
     * @param stepActionKeywords Liste des verbes d'action culinaires à l'impératif (`step_keywords.xml`).
     * @param hangingConnectors Liste des prépositions et connecteurs suspendus (`hanging_connectors.xml`).
     * @param culinaryUnits Liste des unités de mesure culinaires (`culinary_units.xml`).
     * @return La liste finale des consignes d'instructions nettoyées, majusculées et ponctuées.
     */
    fun beautifyInstructions(
        instructions: List<String>, 
        stepActionKeywords: List<String> = emptyList(),
        hangingConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList(),
        spellingRepairs: List<String> = emptyList()
    ): List<String> {
        return Ocr5InstructionBeautifier.beautifyInstructions(
            instructions,
            stepActionKeywords,
            hangingConnectors,
            culinaryUnits,
            spellingRepairs
        )
    }

    /**
     * Effectue la beautification et le regroupement des qualificatifs de variétés d'ingrédients orphelins.
     *
     * @param ingredients La liste des lignes d'ingrédients fusionnées issues de [Ocr4Merger].
     * @param baseIngredients Liste des ingrédients de base attendant un qualificatif (`ingredient_varieties.xml`).
     * @param varietyQualifiers Liste des qualificatifs de variétés (`ingredient_varieties.xml`).
     * @return La liste finale des ingrédients nettoyés, débutant par une majuscule et purgés des en-têtes résiduels.
     */
    fun beautifyIngredients(
        ingredients: List<String>,
        baseIngredients: List<String> = emptyList(),
        varietyQualifiers: List<String> = emptyList()
    ): List<String> {
        return Ocr5IngredientBeautifier.beautifyIngredients(
            ingredients,
            baseIngredients,
            varietyQualifiers
        )
    }
}
