package com.example.chiefinventory.utils

/**
 * Step 5 of the OCR pipeline: Presentation Beautifier.
 * Responsibility: Clean up the final text for professional display (remove artifacts, fix case, ensure punctuation).
 */
object Ocr5Beautifier {

    /**
     * Beautifies a list of merged instructions using action keywords loaded from step_keywords.xml
     * and joining broken lines ending with hanging connectors from hanging_connectors.xml.
     */
    fun beautifyInstructions(
        instructions: List<String>, 
        stepActionKeywords: List<String> = emptyList(),
        hangingConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList()
    ): List<String> {
        return Ocr5InstructionBeautifier.beautifyInstructions(
            instructions,
            stepActionKeywords,
            hangingConnectors,
            culinaryUnits
        )
    }

    /**
     * Beautification des ingrédients (ex : majuscule initiale, regroupement des qualificatifs de variété orphelins)
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
