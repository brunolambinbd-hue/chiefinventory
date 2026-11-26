package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 2.7 : Trieur spatial BoundingBox de la Colonne 1 (Ingrédients).
 *
 * Responsabilités :
 * - Effectuer le tri spatial BoundingBox Y/X uniquement sur les lignes de la Colonne 1 (left < 450 px).
 * - Préserver l'ordre initial des lignes des autres colonnes (Instructions, Vin, left >= 450 px).
 */
object Ocr2DSpatialColumnSorter {

    /**
     * Re-ordonne spatialement les lignes nettoyées de la Colonne 1 (Ingrédients)
     * tout en conservant l'ordre initial des autres colonnes.
     *
     * @param cleanedLines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param lines2D La liste des lignes 2D scannées avec leurs coordonnées BoundingBox.
     * @return La nouvelle liste de lignes de texte avec la Colonne 1 ré-ordonnée géométriquement.
     */
    fun sortColumn1Spatially(
        cleanedLines: List<String>,
        lines2D: List<OcrTextLine>
    ): List<String> {
        if (cleanedLines.isEmpty() || lines2D.isEmpty()) return cleanedLines

        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val cleanedLinesLower = cleanedLines.map { it.trim().lowercase() }.toSet()

        val normalizedLines2D = lines2D.map { line2D ->
            line2D.copy(text = Ocr1Corrector.correct(line2D.text, vocab.ocrSpellingRepairs))
        }.filter { line2D ->
            val cleanT = line2D.text.trim().lowercase()
            cleanT.isNotBlank() && cleanedLinesLower.any { cl ->
                cl == cleanT || cl.contains(cleanT) || cleanT.contains(cl)
            }
        }

        if (normalizedLines2D.isEmpty()) return cleanedLines

        val col1Raw = normalizedLines2D.filter { it.left < 450 }
        val col1InstructionStartTop = col1Raw.filter { l2D ->
            val text = l2D.text.trim()
            val lower = text.lowercase()
            val originalText = lines2D.find { it.left == l2D.left && it.top == l2D.top }?.text?.trim() ?: ""
            originalText.startsWith("•") || originalText.startsWith("*") || originalText.startsWith("-") ||
            text.startsWith("•") || text.startsWith("*") || text.startsWith("-") ||
            Regex("^\\d{1,2}[.)\\-]").containsMatchIn(text) ||
            OcrInstructionUtils.hasActionVerb(text, vocab.stepActionKeywords) ||
            vocab.ocr1ProtectedInstructionsHeaders.any { lower.startsWith(it.lowercase()) }
        }.minOfOrNull { it.top } ?: Int.MAX_VALUE

        val col1Ingredients = mutableListOf<OcrTextLine>()
        val col1Instructions = mutableListOf<OcrTextLine>()
        val col2Lines = mutableListOf<OcrTextLine>()
        val col3Lines = mutableListOf<OcrTextLine>()

        for (line2D in normalizedLines2D) {
            val left = line2D.left

            if (left < 450) {
                if (line2D.top >= col1InstructionStartTop) {
                    col1Instructions.add(line2D)
                } else {
                    col1Ingredients.add(line2D)
                }
            } else if (left < 1200) {
                col2Lines.add(line2D)
            } else {
                col3Lines.add(line2D)
            }
        }

        val sortedCol1Ingredients = Ocr2DSpatialLayoutSorter.sortSingleColumn(col1Ingredients)
        val sortedCol1Instructions = Ocr2DSpatialLayoutSorter.sortSingleColumn(col1Instructions)
        val sortedCol2 = Ocr2DSpatialLayoutSorter.sortSingleColumn(col2Lines)
        val sortedCol3 = Ocr2DSpatialLayoutSorter.sortSingleColumn(col3Lines)

        val combined2D = sortedCol1Ingredients + sortedCol1Instructions + sortedCol2 + sortedCol3
        return combined2D.map { it.text }
    }
}
