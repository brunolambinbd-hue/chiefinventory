package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 4 (Merger) dédié au découpage des blocs d'instructions narratives en phrases individuelles
 * en préservant les points d'abréviations culinaires ("th.", "c.", "temp.", "t.").
 */
object Ocr4MergerSentenceSplitter {

    /**
     * Découpe un bloc de texte d'instructions narratives en phrases individuelles d'après les ponctuations fortes (`.`, `!`, `?`),
     * sans jamais couper après une abréviation culinaire se terminant par un point.
     *
     * @param text Le bloc de texte d'instructions narratives.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La liste des phrases isolées et nettoyées.
     */
    fun splitIntoSentences(
        text: String,
        vocabulary: OcrVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    ): List<String> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return emptyList()

        val abbrevs = vocabulary.culinaryMeasurementUnits
            .filter { it.endsWith(".") }
            .map { it.lowercase().trim() }
            .distinct()

        val rawSplits = trimmed.split(Regex("(?<=[.帖!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val merged = mutableListOf<String>()
        var i = 0
        while (i < rawSplits.size) {
            var curr = rawSplits[i]
            while (i + 1 < rawSplits.size) {
                val nextS = rawSplits[i + 1]
                val words = curr.split(Regex("\\s+"))
                val lastWord = words.lastOrNull()?.lowercase()?.trim() ?: ""
                val endsWithAbbrev = abbrevs.any { abbrev ->
                    val cleanAbbrev = abbrev.removeSuffix(".")
                    lastWord == abbrev || lastWord == cleanAbbrev || lastWord == "$cleanAbbrev."
                }
                if (endsWithAbbrev) {
                    curr = "$curr $nextS"
                    i++
                } else {
                    break
                }
            }
            merged.add(curr)
            i++
        }

        return merged
    }
}
