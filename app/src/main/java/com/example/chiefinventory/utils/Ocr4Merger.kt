package com.example.chiefinventory.utils

import android.util.Log

/**
 * Étape 4 du pipeline OCR : Fusion et Reconstruction Sémantique des Lignes Intermédiaires.
 *
 * Responsabilités :
 * - Fusionner les lignes d'ingrédients ou de consignes tronquées par un saut de ligne ou une césure par tiret (`-`).
 * - Détecter les noms et connecteurs suspendus en fin de ligne (*"de"*, *"du"*, *"vinaigre"*, *"huile"*).
 * - Recoller les qualificatifs de préparation orphelins (*"haché"*, *"émincé"*).
 * - Découper les blocs d'instructions narratives en phrases individuelles ordonnées (une phrase par ligne).
 */
object Ocr4Merger {
    private const val TAG = "Ocr4Merger"

    /** Connecteurs d'ingrédients de liaison provoquant la fusion de la ligne suivante. */
    private val DEFAULT_INGREDIENTS_CONNECTORS = listOf("de", "du", "des", "d'", "au", "aux", "à", "ou", "ou de", "ou d'", "pour", "pour la", "pour le", "pour les")

    /** Noms d'ingrédients appelant presque toujours un complément ("de ..."). */
    private val HANGING_NOUNS = listOf("vinaigre", "huile", "jus", "zeste", "pincée", "pincee", "filet", "brin", "brins", "botte", "gousse", "gousses", "cuillère", "cuillere", "verre")

    /** Articles isolés en fin de ligne indiquant une fusion obligatoire. */
    private val ARTICLES = listOf("le", "la", "les", "l'", "un", "une", "des", "du")

    /**
     * Fusionne les lignes d'ingrédients tronquées et scinde ensuite les lignes combinées.
     *
     * @param rawIngredients La liste des lignes d'ingrédients brutes issues de [Ocr3Categorizer].
     * @param preparationModifiers La liste des adjectifs/modificateurs de préparation (`modifiers.xml`).
     * @param commonIngredients La liste des ingrédients connus sans quantité (`ingredients.xml`).
     * @param mandatoryComplements La liste des compléments d'ingrédients obligatoires (`complement_ingredients.xml`).
     * @return La liste finale des ingrédients reconstitués et séparés.
     */
    fun mergeIngredients(
        rawIngredients: List<String>, 
        preparationModifiers: List<String>, 
        commonIngredients: List<String>,
        mandatoryComplements: List<String> = emptyList(),
        customConnectors: List<String> = emptyList()
    ): List<String> {
        if (rawIngredients.isEmpty()) return emptyList()
        Log.d(TAG, "--- mergeIngredients START ---")

        val intermediateMerged = mutableListOf<String>()
        var current = ""
        var prevOriginalLine = ""

        for (line in rawIngredients) {
            val normalizedLine = Ocr1Normalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            if (current.isEmpty()) {
                current = normalizedLine
                prevOriginalLine = line
                continue
            }

            val isNewStart = startsWithQuantity(normalizedLine)
            val prevIsHanging = isHanging(current, customConnectors)
            val nextStartsWithConnector = startsWithConnector(normalizedLine, customConnectors)
            val nextIsModifier = isOrphanModifier(normalizedLine, preparationModifiers)
            val prevEndsWithPunctuation = current.trim().lastOrNull()?.let { it == ',' || it == '.' } ?: false
            val prevWasHyphenated = prevOriginalLine.trimEnd().endsWith("-")

            val hasUnclosedParen = current.count { it == '(' } > current.count { it == ')' }
            val prevEndsWithRangeConnector = current.lowercase().trimEnd().let { it.endsWith(" ou") || it.endsWith(" à") || it.endsWith(" a") || it.endsWith(" -") || it.endsWith("-") }
            val allowMerge = (!isNewStart || hasUnclosedParen || prevEndsWithRangeConnector) && !prevEndsWithPunctuation && (prevIsHanging || nextStartsWithConnector || prevWasHyphenated || nextIsModifier || prevEndsWithRangeConnector)

            if (allowMerge) {
                if (prevWasHyphenated) {
                    val currentClean = current.trimEnd()
                    val charBeforeDash = if (currentClean.endsWith("-")) {
                        if (currentClean.length >= 2) currentClean[currentClean.length - 2] else null
                    } else {
                        currentClean.lastOrNull()
                    }

                    val endsWithLetter = charBeforeDash?.let { Regex("\\p{L}").matches(it.toString()) } ?: false
                    val startsWithLetter = normalizedLine.firstOrNull()?.let { Regex("\\p{L}").matches(it.toString()) } ?: false

                    if (endsWithLetter && startsWithLetter) {
                        val base = if (currentClean.endsWith("-")) currentClean.dropLast(1) else currentClean
                        current = base.trim() + normalizedLine.trim()
                    } else {
                        val base = current.trimEnd()
                        current = if (base.endsWith(" -") || base.endsWith("- ")) {
                            "$base ${normalizedLine.trim()}"
                        } else if (base.endsWith("-")) {
                            base + normalizedLine.trimStart()
                        } else {
                            "$base ${normalizedLine.trim()}"
                        }
                    }
                } else {
                    val lineToAdd = if (nextStartsWithConnector) normalizedLine.replaceFirstChar { it.lowercase() } else normalizedLine
                    current = "${current.trim()} ${lineToAdd.trim()}"
                }
            } else {
                intermediateMerged.add(current)
                current = normalizedLine
            }
            prevOriginalLine = line
        }
        if (current.isNotEmpty()) intermediateMerged.add(current)

        val finalSplitList = mutableListOf<String>()
        for (mergedIng in intermediateMerged) {
            val splits = OcrHelperUtils.splitCombinedIngredients(
                mergedIng, 
                commonIngredients, 
                mandatoryComplements, 
                preparationModifiers,
                emptyList(),
                emptyList()
            )
            finalSplitList.addAll(splits)
        }

        return finalSplitList
    }

    /**
     * Fusionne les blocs d'instructions narratives tronqués et les découpe en une phrase unique par ligne.
     *
     * @param rawInstructions La liste des lignes d'instructions brutes issues de [Ocr3Categorizer].
     * @return La liste des consignes de préparation reconstruites et séparées phrase par phrase.
     */
    fun mergeInstructions(rawInstructions: List<String>): List<String> {
        if (rawInstructions.isEmpty()) return emptyList()
        Log.d(TAG, "--- mergeInstructions START ---")

        val resultSentences = mutableListOf<String>()
        var currentBlock = ""
        var prevOriginalLine = ""

        for (line in rawInstructions) {
            val normalizedLine = Ocr1Normalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            val currentEndsWithPeriod = currentBlock.trimEnd().let { it.endsWith(".") || it.endsWith("!") || it.endsWith("?") }
            val currentEndsWithConjunction = currentBlock.trimEnd().let { 
                it.endsWith(" et") || it.endsWith(" et-") || it.endsWith(" ou") || it.endsWith(" ou-") || it.endsWith(",") 
            }
            val hasExplicitStepMarker = Regex("^\\s*(?:[•\\-*]|\\d{1,2}\\s*[.)\\-]\\s*)").containsMatchIn(line) || Regex("^\\s*(?:[•\\-*]|\\d{1,2}\\s*[.)\\-]\\s*)").containsMatchIn(normalizedLine)
            val isNextStep = hasExplicitStepMarker || currentEndsWithPeriod || (!currentEndsWithConjunction && (isNewStep(line) || isNewStep(normalizedLine)))

            if (currentBlock.isNotEmpty() && !isNextStep) {
                val prevWasHyphenated = prevOriginalLine.trimEnd().endsWith("-")

                if (prevWasHyphenated) {
                    val blockClean = currentBlock.trimEnd()
                    val charBeforeDash = if (blockClean.endsWith("-")) {
                        if (blockClean.length >= 2) blockClean[blockClean.length - 2] else null
                    } else {
                        blockClean.lastOrNull()
                    }

                    val endsWithLetter = charBeforeDash?.let { Regex("\\p{L}").matches(it.toString()) } ?: false
                    val startsWithLetter = normalizedLine.firstOrNull()?.let { Regex("\\p{L}").matches(it.toString()) } ?: false

                    val baseWord = blockClean.dropLast(1).trim().split(Regex("\\s+")).lastOrNull()?.lowercase() ?: ""
                    val standaloneWords = setOf("et", "ou", "de", "du", "des", "d", "au", "aux", "en", "par", "pour", "sur", "à", "a")
                    val isStandaloneWordHyphenated = baseWord in standaloneWords

                    if (endsWithLetter && startsWithLetter && !isStandaloneWordHyphenated) {
                        val base = if (blockClean.endsWith("-")) blockClean.dropLast(1) else blockClean
                        currentBlock = base.trim() + normalizedLine.trim()
                    } else {
                        val base = if (blockClean.endsWith("-")) blockClean.dropLast(1) else blockClean
                        currentBlock = "${base.trim()} ${normalizedLine.trim()}"
                    }
                } else {
                    currentBlock = "${currentBlock.trim()} ${normalizedLine.trim()}"
                }
            } else {
                if (currentBlock.isNotEmpty()) {
                    resultSentences.addAll(splitIntoSentences(currentBlock))
                }
                currentBlock = normalizedLine
            }
            prevOriginalLine = line
        }

        if (currentBlock.isNotEmpty()) {
            resultSentences.addAll(splitIntoSentences(currentBlock))
        }

        return resultSentences
    }

    /**
     * Découpe un bloc de texte narratif en phrases individuelles d'après les ponctuations fortes (`.`, `!`, `?`).
     *
     * @param text Le bloc de texte narratif.
     * @return La liste des phrases isolées.
     */
    private fun splitIntoSentences(text: String): List<String> {
        val pattern = Regex("(?<=[.!?])\\s+")
        return text.split(pattern).map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Détermine si une ligne d'ingrédient se termine par un mot suspendu ou une quantité incomplète sans nom d'ingrédient.
     *
     * @param text La ligne d'ingrédient à analyser.
     * @return `true` si la ligne est suspendue et doit être fusionnée avec la suivante, `false` sinon.
     */
    private fun isHanging(text: String, customConnectors: List<String> = emptyList()): Boolean {
        val lower = text.lowercase().trim()
        if (ARTICLES.contains(lower)) return true

        // Détection de parenthèse ouverte non refermée ou tiret terminal (ex: "1 citron (1/2 pour le jus -")
        val hasUnclosedParenthesis = text.count { it == '(' } > text.count { it == ')' }
        val endsWithHyphen = text.trimEnd().endsWith("-")
        if (hasUnclosedParenthesis || endsWithHyphen) return true

        val parsed = IngredientParser.parse(text)
        if (parsed.quantity != null && (parsed.name.isBlank() || parsed.name.matches(Regex("(?i)^(?:soupe|café|c\\.?\\s*[àa]\\s*(?:soupe|café)|g|kg|cl|ml|l)$")))) {
            return true
        }

        val connectors = if (customConnectors.isNotEmpty()) customConnectors else DEFAULT_INGREDIENTS_CONNECTORS
        val endsWithConnector = connectors.any { lower.endsWith(" $it") || lower.endsWith("$it") }
        val endsWithHangingNoun = HANGING_NOUNS.any { lower.endsWith(" $it") || lower == it || lower.endsWith("soupe") || lower.endsWith("café") }
        return endsWithConnector || endsWithHangingNoun
    }

    /**
     * Détermine si le texte commence par un connecteur de liaison d'ingrédients (ex: *"de"*, *"du"*, *"d'"*, *"au"*).
     *
     * @param text La ligne à analyser.
     * @return `true` si la ligne commence par un connecteur, `false` sinon.
     */
    private fun startsWithConnector(text: String, customConnectors: List<String> = emptyList()): Boolean {
        val connectors = if (customConnectors.isNotEmpty()) customConnectors else DEFAULT_INGREDIENTS_CONNECTORS
        val lower = text.lowercase().trim()
        return connectors.any { lower.startsWith("$it ") || (it.endsWith("'") && lower.startsWith(it)) }
    }

    /**
     * Détermine si une ligne contient uniquement un adjectif de préparation orphelin (ex: *"haché"*, *"émincé"*).
     *
     * @param text La ligne à évaluer.
     * @param preparationModifiers La liste des modificateurs de préparation (`modifiers.xml`).
     * @return `true` s'il s'agit d'un adjectif orphelin devant être rattaché, `false` sinon.
     */
    private fun isOrphanModifier(text: String, preparationModifiers: List<String>): Boolean {
        val lower = text.lowercase().trim()
        if (lower.startsWith(",") || lower.startsWith("(")) return true
        val wordsCount = lower.split(Regex("\\s+")).size
        if (wordsCount > 5) return false
        val cleanStart = java.text.Normalizer.normalize(lower.replace(Regex("^[^\\p{L}]+"), ""), java.text.Normalizer.Form.NFC)
        return preparationModifiers.any { mod ->
            val cleanMod = java.text.Normalizer.normalize(mod.trim().lowercase(), java.text.Normalizer.Form.NFC)
            cleanStart == cleanMod || cleanStart.startsWith(cleanMod) || cleanMod.startsWith(cleanStart)
        }
    }

    /**
     * Détermine si une ligne commence par une quantité, un article ou une puce typographique.
     *
     * @param line La ligne à analyser.
     * @return `true` si la ligne commence par une quantité ou un article, `false` sinon.
     */
    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:[\\d¼½¾]|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|le\\b|la\\b|les\\b|l['’]|quelques\\b|plusieurs\\b|un peu\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    /**
     * Détermine si la ligne marque le début d'une nouvelle étape de consigne d'instructions.
     *
     * @param line La ligne d'instruction à évaluer.
     * @return `true` s'il s'agit du début d'une nouvelle étape, `false` sinon.
     */
    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*(?:[•\\-*]|\\d{1,2}\\s*[.)\\-]\\s*|Faites\\b|Pendant\\b|Mélangez\\b|Plongez\\b|Egoutez\\b|Égouttez\\b|Servez\\b|Coupez\\b|Préparez\\b|Lavez\\b|Disposez\\b|Ajoutez\\b|Répartissez\\b|Garnissez\\b|Salez\\b|Mettez\\b)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }
}
