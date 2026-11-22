package com.chefinventory.app.utils

import com.chefinventory.app.utils.Ocr5Beautifier.filterBeautifierNoise
import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de l'Étape 5 (Embellissement) dédié à la beautification et au recollage sémantique final des consignes d'instructions.
 *
 * Responsabilités :
 * Empêcher la terminaison artificielle d'une consigne par une conjonction (" et.", " ou.", ",.").
 * Supprimer le point artificiel derrière la conjonction, passer le mot suivant en minuscule ("Garnissez" -> "garnissez"),
 *   et fusionner les deux consignes en une seule phrase fluide.
 * Nettoyer les préfixes parasites d'en-tête devant les verbes d'action.
 * Filtrer les mots d'en-tête de section isolés ("Préparation.", "Instructions.").
 * Consommer les expressions régulières pré-compilées de [OcrRegexes.Beautifier].
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
        spellingRepairs: List<String> = emptyList(),
        offTaxonomyList: List<String> = emptyList()
    ): List<String> {
        val baseInstructions = instructions.filterBeautifierNoise()
        if (baseInstructions.isEmpty()) return emptyList()

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val activeUnits = (if (culinaryUnits.isNotEmpty()) culinaryUnits else vocab.culinaryMeasurementUnits)
            .map { it.lowercase().trim() }
            .distinct()

        val prefixPattern = OcrRegexes.getInstructionPrefixPattern(stepActionKeywords)

        val standaloneTrashWords = (vocab.standaloneArticles + vocab.hangingConnectors).map { it.lowercase().trim() }.toSet()
        val headerTrashWords = vocab.ocr1ProtectedInstructionsHeaders.map { it.lowercase().trim() }.toSet()

        val prefixPatterns = listOf(
            OcrRegexes.Beautifier.BULLET_PREFIX,
            prefixPattern,
            OcrRegexes.Beautifier.STEP_DIGIT_PREFIX,
            OcrRegexes.Beautifier.STEP_NUMBER_BEFORE_CAPITAL
        )

        // 1. Nettoyage, élisions et assainissement initial de chaque ligne
        val cleanedLines = baseInstructions.mapNotNull { line ->
            val cleanLine = OcrInstructionUtils.stripSingleLeadingAlphaOrBulletPrefix(line)
            val lineWithElision = fixFrenchInstructionElisions(cleanLine, vocab, offTaxonomyList)
            val hasAction = OcrInstructionUtils.hasActionVerb(lineWithElision, stepActionKeywords)
            Ocr5Beautifier.sanitizeLine(
                line = lineWithElision,
                spellingRepairs = spellingRepairs,
                prefixPatterns = prefixPatterns,
                vocab = vocab,
                standaloneTrashWords = standaloneTrashWords,
                headerTrashWords = headerTrashWords,
                hasActionVerb = hasAction
            )
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

            current = OcrTextUtils.ensureSentenceEndingPunctuation(current, vocab.sentenceEndingPunctuation)

            mergedInstructions.add(current)
            i++
        }

        return mergedInstructions
    }

    /**
     * Passe 1 (Élision) : Rétablit les apostrophes d'élision françaises oubliées par le scanner de l'appareil photo (ex: "lorange" -> "l'orange", "loignon" -> "l'oignon").
     * Vérifie de manière déterministe que le mot sans le 'l' initial correspond à un nom d'ingrédient connu.
     */
    fun fixFrenchInstructionElisions(
        line: String,
        vocabulary: OcrVocabulary? = null,
        offTaxonomyList: List<String> = emptyList()
    ): String {
        if (line.isBlank()) return line
        val vocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val fullDictionary = (vocab.commonIngredientsNoQty + offTaxonomyList).distinct()
        if (fullDictionary.isEmpty()) return line

        val words = line.split(Regex("\\s+"))
        val corrected = words.map { word ->
            val clean = word.trim().trim('.', ',', ';', ':', '!', '?', '•', '-', '*')
            val lower = clean.lowercase()
            if (lower.startsWith("l") && lower.length >= 4 && !lower.startsWith("l'")) {
                val potentialNoun = lower.substring(1)
                val cleanPotential = OcrLevenshteinUtils.stripAccents(potentialNoun)

                val isKnownIngredientNoun = fullDictionary.any { ingr ->
                    val cleanIngr = OcrLevenshteinUtils.stripAccents(ingr.lowercase())
                    cleanIngr == cleanPotential || cleanIngr.endsWith(" $cleanPotential") || cleanIngr.startsWith("$cleanPotential ")
                }

                if (isKnownIngredientNoun) {
                    val rawNoun = clean.substring(1)
                    val pattern = Regex("(?iu)(?<!\\p{L})l(?=" + Regex.escape(rawNoun) + ")")
                    if (pattern.containsMatchIn(word)) {
                        pattern.replace(word, "l'")
                    } else word
                } else word
            } else word
        }
        return corrected.joinToString(" ")
    }
}
