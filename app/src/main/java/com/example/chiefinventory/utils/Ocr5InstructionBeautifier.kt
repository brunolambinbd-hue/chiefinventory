package com.example.chiefinventory.utils

import android.util.Log

/**
 * Assistant dédié à la beautification et au recollage sémantique final des consignes d'instructions lors de l'Étape 5.
 *
 * Responsabilités :
 * - Empêcher la terminaison artificielle d'une consigne par une conjonction (" et.", " ou.", ",.").
 * - Supprimer le point artificiel derrière la conjonction, passer le mot suivant en minuscule ("Garnissez" -> "garnissez"),
 *   et fusionner les deux consignes en une seule phrase fluide.
 * - Nettoyer les préfixes parasites d'en-tête devant les verbes d'action.
 * - Filtrer les mots d'en-tête de section isolés ("Préparation.", "Instructions.").
 */
object Ocr5InstructionBeautifier {
    private const val TAG = "RecipeOCR"

    /**
     * Effectue la beautification et le recollage final des consignes d'instructions.
     */
    fun beautifyInstructions(
        instructions: List<String>,
        stepActionKeywords: List<String> = emptyList(),
        hangingConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList()
    ): List<String> {
        if (instructions.isEmpty()) return emptyList()

        val baseUnits = listOf(" c.", " c", " cuillère", " cuillères", " cuillere", " cuilleres", " g.", " g", " kg", " cl", " ml", " dl", " l.", " l", " pincée", " gousse")
        val allUnits = (baseUnits + culinaryUnits.map { " ${it.trim().lowercase()}" } + culinaryUnits.map { " ${it.trim().lowercase()}." }).distinct()

        val actionVerbsRegex = if (stepActionKeywords.isNotEmpty()) {
            val distinctKeywords = stepActionKeywords
                .map { it.replace(Regex("\\[[^\\]]+\\]"), "") }
                .filter { it.length >= 3 && it.lowercase() !in listOf("minutes", "heures", "secondes", "environ") }
            distinctKeywords.joinToString("|") { Regex.escape(it) }
        } else {
            "(?!minutes\\b|heures\\b)[\\p{Lu}][\\p{Ll}]+"
        }

        val prefixPattern = Regex("^(?:[\\p{L}]{1,3}\\s+)?(?:\\d{1,2}[.)\\-]?\\s+)*(?=(?:$actionVerbsRegex)\\b)", RegexOption.IGNORE_CASE)

        // 1. Nettoyage initial de chaque ligne
        val cleanedLines = instructions.map { line ->
            var cleaned = line.trim()
            cleaned = cleaned.replace(prefixPattern, "")
            cleaned = cleaned.replace(Regex("^\\d{1,2}[.)\\-]\\s*"), "")
            cleaned = cleaned.replace(Regex("^\\d{1,2}\\s+(?=[\\p{Lu}])"), "")
            cleaned = cleaned.replace(Regex("^[•\\-*]\\s*"), "")
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(".")
            line.isNotBlank() && lower != "préparation" && lower != "preparation" && lower != "instructions" && lower != "consignes"
        }

        // 2. Fusion des phrases se terminant par une conjonction (" et.", " ou.", ",.")
        val mergedInstructions = mutableListOf<String>()
        var i = 0
        while (i < cleanedLines.size) {
            var current = cleanedLines[i].trim()
            if (current.isBlank()) {
                i++
                continue
            }

            while (i + 1 < cleanedLines.size) {
                val nextLine = cleanedLines[i + 1].trim()
                if (nextLine.isBlank()) {
                    i++
                    break
                }

                val currentClean = current.trim()
                val endsWithConjunction = currentClean.endsWith(" et.") || currentClean.endsWith(" et") ||
                        currentClean.endsWith(" ou.") || currentClean.endsWith(" ou") ||
                        currentClean.endsWith(",.") || currentClean.endsWith(",")

                val endsWithUnit = allUnits.any { currentClean.lowercase().endsWith(it) }

                if (endsWithConjunction || endsWithUnit) {
                    val isAbbrevDot = listOf(" c.", " g.", " cl.", " ml.", " dl.", " l.", " kg.").any { currentClean.lowercase().endsWith(it) }
                    val currentNoDot = if (isAbbrevDot) currentClean else currentClean.removeSuffix(".").trim()
                    val uncapitalizedNext = nextLine.replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }
                    current = "$currentNoDot $uncapitalizedNext"
                    Log.d(TAG, "INSTRUCTION BEAUTIFIER: Merged '$currentNoDot' with '$nextLine'")
                    i++
                } else {
                    break
                }
            }

            // Capitalisation initiale et ponctuation finale
            if (current.isNotEmpty()) {
                current = current.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            if (current.isNotEmpty() && !current.endsWith(".") && !current.endsWith("!") && !current.endsWith("?")) {
                current += "."
            }

            mergedInstructions.add(current)
            i++
        }

        return mergedInstructions.filter { it.isNotBlank() }
    }
}
