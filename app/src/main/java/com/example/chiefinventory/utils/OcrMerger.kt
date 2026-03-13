package com.example.chiefinventory.utils

/**
 * Step 4 of the OCR pipeline: Final semantic reconstruction.
 * Responsibility: Merge broken lines, handle hyphenation, and format paragraphs.
 */
object OcrMerger {

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
            val normalizedLine = OcrNormalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            if (current.isEmpty()) {
                current = normalizedLine
                continue
            }

            // On fusionne si ce n'est pas un nouveau départ clair (chiffre/unité)
            // ET qu'on a un indice de coupure (connecteur ou nom en attente)
            val isNewStart = startsWithQuantity(normalizedLine)
            val prevIsHanging = isHanging(current)
            val nextStartsWithConnector = startsWithConnector(normalizedLine)

            if (!isNewStart && (prevIsHanging || nextStartsWithConnector)) {
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
            val normalizedLine = OcrNormalizer.normalize(line)
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

    private fun mergeLines(prev: String, next: String): String {
        val p = prev.trim()
        val n = next.trim()
        
        if (p.endsWith("-") && p.length >= 2 && p[p.length-2].isLetter() && n.firstOrNull()?.isLetter() == true) {
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
        return Regex("^(?:\\d|un\\b|une\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }
}
