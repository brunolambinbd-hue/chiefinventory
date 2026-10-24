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
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Beautifier].
 */
object Ocr5InstructionBeautifier {
    private const val TAG = "RecipeOCR"

    /**
     * Effectue la beautification et le recollage sémantique final des consignes d'instructions.
     *
     * @param instructions La liste des instructions de la recette.
     * @param stepActionKeywords Liste des verbes d'action culinaires (`step_keywords.xml`).
     * @param hangingConnectors Liste des prépositions et connecteurs suspendus (`hanging_connectors.xml`).
     * @param culinaryUnits Liste des unités de mesure culinaires (`culinary_units.xml`).
     * @return La liste nettoyée des consignes d'instructions.
     */
    fun beautifyInstructions(
        instructions: List<String>,
        stepActionKeywords: List<String> = emptyList(),
        hangingConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList(),
        spellingRepairs: List<String> = emptyList()
    ): List<String> {
        if (instructions.isEmpty()) return emptyList()

        val defaultUnits = listOf("c.", "c", "cuillère", "cuillères", "cuillere", "cuilleres", "g.", "g", "kg", "cl", "ml", "dl", "l.", "l", "pincée", "gousse")
        val activeUnits = (if (culinaryUnits.isNotEmpty()) culinaryUnits else defaultUnits)
            .map { it.lowercase().trim() }
            .distinct()

        val prefixPattern = OcrRegexes.getInstructionPrefixPattern(stepActionKeywords)

        val standaloneTrashWords = setOf(
            "les", "le", "la", "des", "du", "de", "d'", "un", "une", "et", "ou", "pour", "sur", "avec", "dans", "par", "ce", "ces", "cette", "salade"
        )

        // 1. Nettoyage initial de chaque ligne
        val cleanedLines = instructions.map { line ->
            var cleaned = line.trim()
            if (spellingRepairs.isNotEmpty()) {
                cleaned = Ocr1Normalizer.applySpellingRepairs(cleaned, spellingRepairs)
            }
            cleaned = cleaned.replace(prefixPattern, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.STEP_DIGIT_PREFIX, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.STEP_NUMBER_BEFORE_CAPITAL, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "")
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(".")
            val hasAction = OcrInstructionUtils.hasActionVerb(line, stepActionKeywords)
            val isRecipeHeaderTrash = !hasAction && (lower.contains("recette") || lower.contains("recettes") || line.contains("Ə"))
            line.isNotBlank() && lower != "préparation" && lower != "preparation" && lower != "instructions" && lower != "consignes" && lower !in standaloneTrashWords && !isRecipeHeaderTrash
        }

        // 2. Fusion des phrases se terminant par une conjonction (" et.", " ou.", ",.") ou une unité isolée
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
                val currentLower = currentClean.lowercase()

                val endsWithConjunction = currentClean.endsWith(" et.") || currentClean.endsWith(" et") ||
                        currentClean.endsWith(" ou.") || currentClean.endsWith(" ou") ||
                        currentClean.endsWith(",.") || currentClean.endsWith(",")

                val endsWithUnit = activeUnits.any { unit ->
                    currentLower.endsWith(" $unit") || currentLower.endsWith(" $unit.")
                }

                if (endsWithConjunction || endsWithUnit) {
                    val isAbbrevDot = currentClean.endsWith(".") && activeUnits.any { unit ->
                        unit.endsWith(".") && currentLower.endsWith(" $unit")
                    }
                    val currentNoDot = if (isAbbrevDot) currentClean else currentClean.removeSuffix(".").trim()
                    val uncapitalizedNext = nextLine.replaceFirstChar { it.lowercase() }
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

        return mergedInstructions.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(".")
            line.isNotBlank() && lower !in standaloneTrashWords
        }
    }
}
