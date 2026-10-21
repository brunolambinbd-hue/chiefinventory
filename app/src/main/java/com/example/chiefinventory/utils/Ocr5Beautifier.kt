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
        hangingConnectors: List<String> = emptyList()
    ): List<String> {
        val baseConnectors = listOf("et", "ou", "à", "a", "de", "du", "des", "d", "d'", "avec", "dans", "pour", "sur", "au", "aux", "en", "par")
        val allHangingConnectors = (baseConnectors + hangingConnectors.map { it.lowercase() }).distinct()

        // NIVEAU A : Recollage des lignes d'instructions interrompues par un connecteur pendant
        val joinedInstructions = mutableListOf<String>()
        var i = 0
        while (i < instructions.size) {
            var current = instructions[i].trim()
            if (current.isBlank()) {
                i++
                continue
            }

            while (i + 1 < instructions.size) {
                val nextLine = instructions[i + 1].trim()
                if (nextLine.isBlank()) {
                    i++
                    break
                }

                val currentCleanNoPunct = current.replace(Regex("[.!?]+$"), "").trim()
                val lastWord = currentCleanNoPunct.split(Regex("\\s+")).lastOrNull()?.lowercase()?.replace(Regex("[^\\p{L}'’]"), "") ?: ""

                val endsWithConnector = lastWord in allHangingConnectors || currentCleanNoPunct.endsWith(",") || currentCleanNoPunct.endsWith("-")

                if (endsWithConnector) {
                    val uncapitalizedNext = nextLine.replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }
                    current = "$currentCleanNoPunct $uncapitalizedNext"
                    i++
                } else {
                    break
                }
            }
            joinedInstructions.add(current)
            i++
        }

        val actionVerbsRegex = if (stepActionKeywords.isNotEmpty()) {
            val distinctKeywords = stepActionKeywords
                .map { it.replace(Regex("\\[[^\\]]+\\]"), "") }
                .filter { it.length >= 3 && it.lowercase() !in listOf("minutes", "heures", "secondes", "environ") }
            distinctKeywords.joinToString("|") { Regex.escape(it) }
        } else {
            "(?!minutes\\b|heures\\b)[\\p{Lu}][\\p{Ll}]+"
        }

        val prefixPattern = Regex("^(?:[\\p{L}]{1,3}\\s+)?(?:\\d{1,2}[.)\\-]?\\s+)*(?=(?:$actionVerbsRegex)\\b)", RegexOption.IGNORE_CASE)

        return joinedInstructions.map { line ->
            var cleaned = line.trim()

            // 0. Supprime les préfixes parasites courts et numéros d'étape devant un verbe d'action
            cleaned = cleaned.replace(prefixPattern, "")

            // 1. Supprime les numéros d'étape avec ponctuation : "1.", "1)", "1-"
            cleaned = cleaned.replace(Regex("^\\d{1,2}[.)\\-]\\s*"), "")

            // 2. Supprime un chiffre isolé suivi d'un espace SI le mot suivant commence par une majuscule
            cleaned = cleaned.replace(Regex("^\\d{1,2}\\s+(?=[\\p{Lu}])"), "")

            // 3. Supprime les puces résiduelles (•, -, *)
            cleaned = cleaned.replace(Regex("^[•\\-*]\\s*"), "")

            // NIVEAU B : Sécurité - Nettoyage des connecteurs pendants orphelins en fin de ligne avant ponctuation
            val connectorPattern = allHangingConnectors.joinToString("|") { Regex.escape(it) }
            cleaned = cleaned.replace(Regex("(?i)\\s+(?:$connectorPattern)[.!?\\s]*$"), ".")

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
     * Optionnel : Beautification des ingrédients si nécessaire (ex : majuscule initiale)
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
