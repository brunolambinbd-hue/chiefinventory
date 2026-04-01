package com.example.chiefinventory.utils

/**
 * Step 5 of the OCR pipeline: Presentation Beautifier.
 * Responsibility: Clean up the final text for professional display (remove artifacts, fix case, ensure punctuation).
 */
object Ocr5Beautifier {

    /**
     * Beautifies a list of merged instructions.
     */
    fun beautifyInstructions(instructions: List<String>): List<String> {
        return instructions.map { line ->
            var cleaned = line.trim()

            // 1. Supprime les numéros d'étape avec ponctuation : "1.", "1)", "1-"
            cleaned = cleaned.replace(Regex("^\\d{1,2}[.)\\-]\\s*"), "")

            // 2. Supprime un chiffre isolé suivi d'un espace SI le mot suivant commence par une majuscule
            // (ex: "1 Plongez" -> "Plongez", mais garde "10 minutes")
            cleaned = cleaned.replace(Regex("^\\d{1,2}\\s+(?=[\\p{Lu}])"), "")

            // 3. Supprime les puces résiduelles (•, -, *)
            cleaned = cleaned.replace(Regex("^[•\\-*]\\s*"), "")

            // 4. Force la majuscule au début de la phrase
            if (cleaned.isNotEmpty()) {
                cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

            // 5. Assure un point final si manquant (sauf si ponctuation existante)
            if (cleaned.isNotEmpty() && !cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
                cleaned += "."
            }

            cleaned
        }.filter { it.isNotBlank() }
    }

    /**
     * Optionnel: Beautification des ingrédients si nécessaire (ex: majuscule initiale)
     */
    fun beautifyIngredients(ingredients: List<String>): List<String> {
        return ingredients.map { line ->
            var cleaned = line.trim()
            if (cleaned.isNotEmpty()) {
                cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            cleaned
        }.filter { it.isNotBlank() }
    }
}
