package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de l'Étape 5 (Embellissement) dédié à la beautification et au recollage sémantique final des consignes d'instructions.
 *
 * Responsabilités :
 * - Empêcher la terminaison artificielle d'une consigne par une conjonction (" et.", " ou.", ",.").
 * - Supprimer le point artificiel derrière la conjonction, passer le mot suivant en minuscule ("Garnissez" -> "garnissez"),
 *   et fusionner les deux consignes en une seule phrase fluide.
 * - Nettoyer les préfixes parasites d'en-tête devant les verbes d'action.
 * - Filtrer les mots d'en-tête de section isolés ("Préparation.", "Instructions.").
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Beautifier].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5BeautifierInstruction {
    private const val TAG = "RecipeOCR"
    private const val TRAILING_COMMA_DOT = ",."
    private const val TRAILING_COMMA = ","

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

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val activeUnits = (if (culinaryUnits.isNotEmpty()) culinaryUnits else vocab.culinaryMeasurementUnits)
            .map { it.lowercase().trim() }
            .distinct()

        val prefixPattern = OcrRegexes.getInstructionPrefixPattern(stepActionKeywords)

        val standaloneTrashWords = (vocab.standaloneArticles + vocab.hangingConnectors).map { it.lowercase().trim() }.toSet()
        val headerTrashWords = vocab.ocr1ProtectedInstructionsHeaders.map { it.lowercase().trim() }.toSet()

        // 1. Nettoyage initial de chaque ligne
        val cleanedLines = instructions.map { line ->
            var cleaned = line.trim()
            if (spellingRepairs.isNotEmpty()) {
                cleaned = Ocr1Corrector.applySpellingRepairs(cleaned, spellingRepairs)
            }
            cleaned = cleaned.replace(prefixPattern, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.STEP_DIGIT_PREFIX, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.STEP_NUMBER_BEFORE_CAPITAL, "")
            cleaned = cleaned.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "")
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(".")
            val lowerCondensed = lower.replace(" ", "")
            val hasAction = OcrInstructionUtils.hasActionVerb(line, stepActionKeywords)
            val isOcrNoise = OcrVocabularyLoader.loadFromProjectResources().ocrNoiseExclusions.any { noise ->
                noise.isNotBlank() && line.contains(noise, ignoreCase = true)
            }
            val isRecipeHeaderTrash = if (!hasAction) {
                lower.contains("recette") || isOcrNoise
            } else {
                false
            }
            val isShortNoise = !hasAction && line.none { it.isDigit() } && (lowerCondensed.length <= 3 || lowerCondensed in standaloneTrashWords)
            line.isNotEmpty() && lower !in headerTrashWords && lower !in standaloneTrashWords && !isRecipeHeaderTrash && !isShortNoise
        }

        // 2. Fusion des phrases se terminant par une conjonction (" et.", " ou.", ",.") ou une unité isolée
        val mergedInstructions = mutableListOf<String>()
        var i = 0
        while (i < cleanedLines.size) {
            var current = cleanedLines[i].trim()

            while (i + 1 < cleanedLines.size) {
                val nextLine = cleanedLines[i + 1].trim()
                val currentClean = current.trim()
                val currentLower = currentClean.lowercase()

                val sentenceConjunctions = OcrVocabularyLoader.loadFromProjectResources().ocrSentenceConjunctions.map { it.trim().lowercase() }
                val matchesConj = sentenceConjunctions.any { conj ->
                    currentClean.endsWith(" $conj.") || currentClean.endsWith(" $conj")
                }

                val endsWithConjunction = matchesConj ||
                        currentClean.endsWith(TRAILING_COMMA_DOT) || currentClean.endsWith(TRAILING_COMMA) ||
                        standaloneTrashWords.any { word -> currentLower.endsWith(" $word") || currentLower.endsWith(" $word.") }

                val endsWithUnit = activeUnits.any { unit ->
                    currentLower.endsWith(" $unit") || currentLower.endsWith(" $unit.")
                }

                if (endsWithConjunction || endsWithUnit) {
                    val isAbbrevDot = currentClean.endsWith(".") && activeUnits.any { unit ->
                        unit.endsWith(".") && currentLower.endsWith(" $unit")
                    }
                    val currentNoDot = if (isAbbrevDot) currentClean else currentClean.removeSuffix(".").trim()
                    val firstChar = nextLine[0]
                    val uncapitalizedNext = if (firstChar.isUpperCase()) {
                        firstChar.lowercase() + nextLine.substring(1)
                    } else {
                        nextLine
                    }
                    current = "$currentNoDot $uncapitalizedNext"
                    LogManager.d(TAG, String.format(vocab.logOcr5BeautifierInstructionMerged, currentNoDot, nextLine))
                    i++
                } else {
                    break
                }
            }

            // Capitalisation initiale et ponctuation finale
            val first = current[0]
            if (first.isLowerCase()) {
                current = first.titlecase() + current.substring(1)
            } else {
                // Le premier caractère est déjà une majuscule
            }

            val lastChar = current.last()
            if (lastChar != '.' && lastChar != '!' && lastChar != '?') {
                current += "."
            }

            mergedInstructions.add(current)
            i++
        }

        return mergedInstructions
    }
}
