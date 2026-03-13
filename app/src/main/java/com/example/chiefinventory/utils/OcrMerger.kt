package com.example.chiefinventory.utils

import android.content.res.Resources

/**
 * Step 4 of the OCR pipeline: Final semantic reconstruction.
 * Responsibility: Merge broken lines, handle hyphenation, and format paragraphs.
 */
object OcrMerger {

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

            // On fusionne si la ligne actuelle ne commence pas par une quantité/bullet
            // ou si la ligne précédente semble inachevée (syntaxe brisée)
            if (current.isNotEmpty() && !startsWithQuantity(line)) {
                current = mergeLines(current, normalizedLine)
            } else {
                if (current.isNotEmpty()) merged.add(current)
                current = normalizedLine
            }
        }
        if (current.isNotEmpty()) merged.add(current)
        return merged
    }

    /**
     * Merges narrative instructions into coherent steps.
     */
    fun mergeInstructions(rawInstructions: List<String>): List<String> {
        if (rawInstructions.isEmpty()) return emptyList()

        val mergedSteps = mutableListOf<String>()
        var currentStep = ""

        for (line in rawInstructions) {
            val normalizedLine = OcrNormalizer.normalize(line)
            if (normalizedLine.isEmpty()) continue

            // On fusionne si ce n'est pas un nouveau point (•, -, chiffre)
            // et si la ligne précédente ne finit pas par une ponctuation forte
            if (currentStep.isNotEmpty() && !isNewStep(line) && !isSentenceEnd(currentStep)) {
                currentStep = mergeLines(currentStep, normalizedLine)
            } else {
                if (currentStep.isNotEmpty()) mergedSteps.add(currentStep)
                currentStep = normalizedLine
            }
        }
        if (currentStep.isNotEmpty()) mergedSteps.add(currentStep)
        return mergedSteps
    }

    /**
     * Core merging logic: handle hyphenation and spaces.
     */
    private fun mergeLines(prev: String, next: String): String {
        // Règle 1: Fusion des mots coupés par un tiret (césure)
        if (prev.endsWith("-")) {
            return prev.dropLast(1) + next
        }
        // Règle 2: Espace normal entre deux morceaux de phrase
        return "$prev $next"
    }

    private fun startsWithQuantity(line: String): Boolean {
        return Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|[|Il!](?=[\\s\\d]))", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun isNewStep(line: String): Boolean {
        return Regex("^\\s*[•\\-*\\d]").containsMatchIn(line)
    }

    private fun isSentenceEnd(text: String): Boolean {
        val last = text.trim().lastOrNull() ?: return false
        return last == '.' || last == '!' || last == '?' || last == ':'
    }
}
