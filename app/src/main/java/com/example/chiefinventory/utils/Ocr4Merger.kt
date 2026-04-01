package com.example.chiefinventory.utils

import android.util.Log

/**
 * Step 4 of the OCR pipeline: Final semantic reconstruction.
 * Responsibility: Merge broken lines, handle hyphenation, and format paragraphs.
 */
object Ocr4Merger {
    private const val TAG = "Ocr4Merger"
    private val INGREDIENT_CONNECTORS = listOf("de", "du", "des", "d'", "au", "aux", "à")

    // Noms qui appellent presque toujours un complément "de ..."
    private val HANGING_NOUNS = listOf("vinaigre", "huile", "jus", "zeste", "pincée", "pincee", "filet", "brin", "brins", "botte", "gousse", "gousses", "cuillère", "cuillere", "verre")

    // Articles qui, s'ils sont seuls sur une ligne, indiquent une fusion obligatoire
    private val ARTICLES = listOf("le", "la", "les", "l'", "un", "une", "des", "du")

    /**
     * Merges broken lines of ingredients into a clean list.
     */
    fun mergeIngredients(rawIngredients: List<String>, preparationModifiers: List<String>, commonIngredients: List<String>): List<String> {
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
            val prevIsHanging = isHanging(current)
            val nextStartsWithConnector = startsWithConnector(normalizedLine)
            val nextIsModifier = isOrphanModifier(normalizedLine, preparationModifiers)
            val prevEndsWithPunctuation = current.trim().lastOrNull()?.let { it == ',' || it == '.' } ?: false
            val prevWasHyphenated = prevOriginalLine.trimEnd().endsWith("-")

            if (!isNewStart && !prevEndsWithPunctuation && (prevIsHanging || nextStartsWithConnector || prevWasHyphenated || nextIsModifier)) {
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
                        current = "${current.trim()} ${normalizedLine.trim()}"
                    }
                } else {
                    current = "${current.trim()} ${normalizedLine.trim()}"
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
            val splits = OcrHelperUtils.splitCombinedIngredients(mergedIng, commonIngredients)
            finalSplitList.addAll(splits)
        }

        return finalSplitList
    }

    /**
     * Merges narrative instructions and splits them into one sentence per line.
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

            if (currentBlock.isNotEmpty() && !isNewStep(line)) {
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
                    
                    currentBlock = if (endsWithLetter && startsWithLetter) {
                        val base = if (blockClean.endsWith("-")) blockClean.dropLast(1) else blockClean
                        base.trim() + normalizedLine.trim()
                    } else {
                        currentBlock.trim() + " " + normalizedLine.trim()
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

    private fun splitIntoSentences(text: String): List<String> {
        val pattern = Regex("(?<=[.!?])\\s+")
        return text.split(pattern).map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun isHanging(text: String): Boolean {
        val lower = text.lowercase().trim()
        
        // NOUVEAU : Un article seul sur une ligne est forcément "pendant"
        if (ARTICLES.contains(lower)) return true
        
        val endsWithConnector = INGREDIENT_CONNECTORS.any { lower.endsWith(" $it") || lower.endsWith("$it") }
        val endsWithHangingNoun = HANGING_NOUNS.any { lower.endsWith(" $it") || lower == it }
        return endsWithConnector || endsWithHangingNoun
    }

    private fun startsWithConnector(text: String): Boolean {
        val lower = text.lowercase().trim()
        return INGREDIENT_CONNECTORS.any { lower.startsWith("$it ") || (it.endsWith("'") && lower.startsWith(it)) }
    }

    private fun isOrphanModifier(text: String, preparationModifiers: List<String>): Boolean {
        val lower = text.lowercase().trim()
        if (lower.startsWith(",") || lower.startsWith("(")) return true
        val wordsCount = lower.split(Regex("\\s+")).size
        if (wordsCount > 5) return false
        val cleanStart = lower.replace(Regex("^[^\\p{L}]+"), "")
        return preparationModifiers.any { cleanStart.startsWith(it.lowercase()) }
    }

    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:\\d|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|le\\b|la\\b|les\\b|l['’]|quelques\\b|plusieurs\\b|un peu\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }
}
