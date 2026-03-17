package com.example.chiefinventory.utils

/**
 * Step 4 of the OCR pipeline: Final semantic reconstruction.
 * Responsibility: Merge broken lines, handle hyphenation, and format paragraphs.
 */
object Ocr4Merger {

    private val INGREDIENT_CONNECTORS = listOf("de", "du", "des", "d'", "au", "aux", "à")
    
    // Noms qui appellent presque toujours un complément "de ..."
    private val HANGING_NOUNS = listOf("vinaigre", "huile", "jus", "zeste", "pincée", "pincee", "filet", "brin", "brins", "botte", "gousse", "gousses", "cuillère", "cuillere", "verre")

    /**
     * Merges broken lines of ingredients into a clean list.
     */
    fun mergeIngredients(rawIngredients: List<String>): List<String> {
        if (rawIngredients.isEmpty()) return emptyList()
        
        val merged = mutableListOf<String>()
        var current = ""

        for (line in rawIngredients) {
            val normalizedLine = Ocr1Normalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            if (current.isEmpty()) {
                current = normalizedLine
                continue
            }

            val isNewStart = startsWithQuantity(normalizedLine)
            val prevIsHanging = isHanging(current)
            val nextStartsWithConnector = startsWithConnector(normalizedLine)
            val prevEndsWithPunctuation = current.trim().lastOrNull()?.let { it == ',' || it == '.' } ?: false
            
            // On ajoute la détection du tiret comme signal de fusion
            val prevIsHyphenated = current.trim().endsWith("-")

            if (!isNewStart && !prevEndsWithPunctuation && (prevIsHanging || nextStartsWithConnector || prevIsHyphenated)) {
                current = mergeLines(current, normalizedLine)
            } else {
                merged.add(current)
                current = normalizedLine
            }
        }
        if (current.isNotEmpty()) merged.add(current)
        return merged
    }

    /**
     * Merges narrative instructions and splits them into one sentence per line.
     */
    fun mergeInstructions(rawInstructions: List<String>): List<String> {
        if (rawInstructions.isEmpty()) return emptyList()

        val resultSentences = mutableListOf<String>()
        var currentBlock = ""

        for (line in rawInstructions) {
            val normalizedLine = Ocr1Normalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            if (currentBlock.isNotEmpty() && !isNewStep(line)) {
                currentBlock = mergeLines(currentBlock, normalizedLine)
            } else {
                if (currentBlock.isNotEmpty()) {
                    resultSentences.addAll(splitIntoSentences(currentBlock))
                }
                currentBlock = normalizedLine
            }
        }
        
        if (currentBlock.isNotEmpty()) {
            resultSentences.addAll(splitIntoSentences(currentBlock))
        }

        return resultSentences
    }

    private fun splitIntoSentences(text: String): List<String> {
        val pattern = Regex("(?<=[.!?])\\s+")
        return text.split(pattern)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Core merging logic: handles hyphenation (césure) and structural spaces.
     */
    private fun mergeLines(prev: String, next: String): String {
        val p = prev.trim()
        val n = next.trim()
        
        // Règle de fusion des mots coupés par un tiret
        val letterPattern = Regex("^\\p{L}")
        val prevHasLetterBeforeDash = p.length >= 2 && (p[p.length - 2].isLetter() || Regex("\\p{L}").matches(p[p.length - 2].toString()))
        val nextStartsWithLetter = letterPattern.containsMatchIn(n)
        
        if (p.endsWith("-") && prevHasLetterBeforeDash && nextStartsWithLetter) {
            return p.dropLast(1) + n
        }
        
        return "$p $n"
    }

    private fun isHanging(text: String): Boolean {
        val lower = text.lowercase().trim()
        val endsWithConnector = INGREDIENT_CONNECTORS.any { lower.endsWith(" $it") || lower.endsWith("$it") }
        val endsWithHangingNoun = HANGING_NOUNS.any { lower.endsWith(" $it") || lower == it }
        return endsWithConnector || endsWithHangingNoun
    }

    private fun startsWithConnector(text: String): Boolean {
        val lower = text.lowercase().trim()
        return INGREDIENT_CONNECTORS.any { lower.startsWith("$it ") || (it.endsWith("'") && lower.startsWith(it)) }
    }

    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:\\d|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|quelques\\b|plusieurs\\b|un peu\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }
}
