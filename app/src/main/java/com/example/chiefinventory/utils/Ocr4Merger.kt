package com.example.chiefinventory.utils

/**
 * Step 4 of the OCR pipeline: Final semantic reconstruction.
 * Responsibility: Merge broken lines, handle hyphenation, and format paragraphs.
 */
object Ocr4Merger {

    private val INGREDIENT_CONNECTORS = listOf("de", "du", "des", "d'", "au", "aux", "à")
    
    // Noms qui appellent presque toujours un complément "de ..."
    private val HANGING_NOUNS = listOf("vinaigre", "huile", "jus", "zeste", "pincée", "pincee", "filet", "brin", "brins", "botte", "gousse", "gousses", "cuillère", "cuillere", "verre")

    // Modificateurs de préparation qui sont souvent coupés sur une nouvelle ligne
    private val PREPARATION_MODIFIERS = listOf(
        "haché", "hachée", "hachés", "hachées",
        "émincé", "émincée", "émincés", "émincées",
        "ciselé", "ciselée", "ciselés", "ciselées",
        "coupé", "coupée", "coupés", "coupées",
        "râpé", "râpée", "râpés", "râpées",
        "frais", "fraîche", "fraîches",
        "fondue", "fondu", "fondus", "fondues",
        "concasse", "concassé", "concassée",
        "pressé", "pressée", "découpé", "découpée",
        "égoutté", "egoutté", "nettoyé", "moulu",
        "en dés", "en rondelles", "en tranches", "en cubes",
        "truite", "salade", "frais haché", "frais hachée", "frais hachés", "frais hachées"
    )

    /**
     * Merges broken lines of ingredients into a clean list.
     */
    fun mergeIngredients(rawIngredients: List<String>): List<String> {
        if (rawIngredients.isEmpty()) return emptyList()
        
        val merged = mutableListOf<String>()
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
            val nextIsModifier = isOrphanModifier(normalizedLine)
            val prevEndsWithPunctuation = current.trim().lastOrNull()?.let { it == ',' || it == '.' } ?: false
            val prevWasHyphenated = prevOriginalLine.trim().endsWith("-")

            // Fusion si : Connecteur OR Tiret OR Modificateur orphelin (ex: haché)
            if (!isNewStart && !prevEndsWithPunctuation && (prevIsHanging || nextStartsWithConnector || prevWasHyphenated || nextIsModifier)) {
                if (prevWasHyphenated) {
                    val trimmedOriginal = prevOriginalLine.trim()
                    val charBeforeDash = if (trimmedOriginal.length >= 2) trimmedOriginal[trimmedOriginal.length - 2] else ' '
                    val isCesure = Regex("\\p{L}").matches(charBeforeDash.toString()) && 
                                  Regex("^\\p{L}").containsMatchIn(normalizedLine)
                    
                    current = if (isCesure) {
                        current.trim() + normalizedLine.trim()
                    } else {
                        current.trim() + "- " + normalizedLine.trim()
                    }
                } else {
                    current = mergeLines(current, normalizedLine)
                }
            } else {
                merged.add(current)
                current = normalizedLine
            }
            prevOriginalLine = line
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
        var prevOriginalLine = ""

        for (line in rawInstructions) {
            val normalizedLine = Ocr1Normalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            if (currentBlock.isNotEmpty() && !isNewStep(line)) {
                val prevWasHyphenated = prevOriginalLine.trim().endsWith("-")
                
                if (prevWasHyphenated) {
                    val trimmedOriginal = prevOriginalLine.trim()
                    val charBeforeDash = if (trimmedOriginal.length >= 2) trimmedOriginal[trimmedOriginal.length - 2] else ' '
                    val isCesure = Regex("\\p{L}").matches(charBeforeDash.toString()) && 
                                  Regex("^\\p{L}").containsMatchIn(normalizedLine)
                    
                    currentBlock = if (isCesure) {
                        currentBlock.trim() + normalizedLine.trim()
                    } else {
                        currentBlock.trim() + "- " + normalizedLine.trim()
                    }
                } else {
                    currentBlock = mergeLines(currentBlock, normalizedLine)
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
        return text.split(pattern)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun mergeLines(prev: String, next: String): String {
        return "${prev.trim()} ${next.trim()}"
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

    private fun isOrphanModifier(text: String): Boolean {
        val lower = text.lowercase().trim()
        if (lower.startsWith(",")) return true
        
        // Si la ligne est courte et commence par un mot de préparation
        val words = lower.split(Regex("\\s+"))
        if (words.size > 3) return false
        
        return PREPARATION_MODIFIERS.any { lower.startsWith(it) }
    }

    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:\\d|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|quelques\\b|plusieurs\\b|un peu\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }
}
