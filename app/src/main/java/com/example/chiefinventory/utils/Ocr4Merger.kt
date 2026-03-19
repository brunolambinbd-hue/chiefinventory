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
     * @param rawIngredients The list of ingredient lines to merge.
     * @param preparationModifiers The list of modifiers (from XML) that trigger a merge.
     */
    fun mergeIngredients(rawIngredients: List<String>, preparationModifiers: List<String>): List<String> {
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
            val nextIsModifier = isOrphanModifier(normalizedLine, preparationModifiers)
            val prevEndsWithPunctuation = current.trim().lastOrNull()?.let { it == ',' || it == '.' } ?: false
            
            // On vérifie si la ligne originale précédente se terminait par un tiret
            val prevWasHyphenated = prevOriginalLine.trim().endsWith("-")

            if (!isNewStart && !prevEndsWithPunctuation && (prevIsHanging || nextStartsWithConnector || prevWasHyphenated || nextIsModifier)) {
                if (prevWasHyphenated) {
                    val trimmedOriginal = prevOriginalLine.trim()
                    val charBeforeDash = if (trimmedOriginal.length >= 2) trimmedOriginal[trimmedOriginal.length - 2] else ' '
                    
                    // Logique simplifiée pour la césure : si ça finit et commence par une lettre, on colle
                    val endsWithLetter = current.lastOrNull()?.isLetter() == true
                    val startsWithLetter = normalizedLine.firstOrNull()?.isLetter() == true
                    
                    current = if (endsWithLetter && startsWithLetter) {
                        current.trim() + normalizedLine.trim()
                    } else {
                        // Pas une césure (ex: Zone 5-), on garde le tiret et on ajoute un espace
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
                    val endsWithLetter = currentBlock.lastOrNull()?.isLetter() == true
                    val startsWithLetter = normalizedLine.firstOrNull()?.isLetter() == true

                    currentBlock = if (endsWithLetter && startsWithLetter) {
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

    private fun isOrphanModifier(text: String, preparationModifiers: List<String>): Boolean {
        val lower = text.lowercase().trim()
        
        // Une ligne commençant par une virgule ou une parenthèse est TOUJOURS une suite d'ingrédient
        if (lower.startsWith(",") || lower.startsWith("(")) return true
        
        // Si la ligne est courte (jusqu'à 5 mots)
        val words = lower.split(Regex("\\s+"))
        if (words.size > 5) return false
        
        // On nettoie la ponctuation de début pour matcher le dictionnaire (ex: (moulu -> moulu)
        val cleanStart = lower.replace(Regex("^[^\\p{L}]+"), "")
        
        return preparationModifiers.any { cleanStart.startsWith(it.lowercase()) }
    }

    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:\\d|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|quelques\\b|plusieurs\\b|un peu\\b|•|\\-|\\*)", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }
}
