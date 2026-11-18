package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de l'Étape 3 (Pré-processeur 2D) réutilisant 100% des fonctions existantes du projet
 * ([Ocr1Corrector], [RecipeMetadataParser], [IngredientParser], [OcrInstructionUtils])
 * pour ré-ordonner spatialement en 1D les fiches de recettes à 2 colonnes sans en-tête d'ingrédients.
 */
object Ocr3SpatialLineReorderer {

    private const val TAG = "RecipeOCR"

    /**
     * Reconstitue une liste 1D propre et ré-ordonnée (Colonne 1 Gauche, Colonne 2 Droite, Instructions)
     * en réutilisant les fonctions d'ingrédients et de méta-données existantes.
     *
     * @param lines2D La liste des lignes 2D nettoyées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param results L'objet [RawSections] alimenté avec les méta-données extraites.
     * @return La liste 1D de lignes ré-ordonnées prête pour [Ocr3Categorizer.categorize].
     */
    fun reorder2DLines(
        lines2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): List<String> {
        if (lines2D.isEmpty()) return emptyList()

        val actionKeywords = vocabulary.stepActionKeywords
        val startVerbs = vocabulary.stepStartVerbs
        val instructionHeaders = vocabulary.ocr1ProtectedInstructionsHeaders.map { it.lowercase().trim() }
        val repairs = vocabulary.ocrSpellingRepairs

        // 1. Pré-analyse dynamique de la position du premier verbe d'action (réutilisation de OcrInstructionUtils)
        val firstInstructionLine = lines2D.firstOrNull { line ->
            val trimmed = Ocr1Corrector.normalize(line.text, repairs).trim()
            val lower = trimmed.lowercase()
            val metaCheck = RecipeMetadataParser.extractMetadata(trimmed, results, vocabulary, line.bitmap, line.bounds)
            val isMetaLine = metaCheck.isPureMetadata || OcrRegexes.Metadata.TIME_COMBINED.containsMatchIn(trimmed) ||
                    OcrRegexes.Metadata.TIME_PREFIX.containsMatchIn(trimmed) || OcrRegexes.Metadata.TIME_SUFFIX.containsMatchIn(trimmed)
            if (isMetaLine) return@firstOrNull false

            val isInstructionHeader = instructionHeaders.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it:") }
            val hasStartVerb = OcrInstructionUtils.hasStartVerb(trimmed, startVerbs)
            val hasActionVerb = OcrInstructionUtils.hasActionVerb(trimmed, actionKeywords)
            isInstructionHeader || hasStartVerb || (hasActionVerb && trimmed.length > 20)
        }

        val instructionTopLimit = firstInstructionLine?.top ?: Int.MAX_VALUE

        // 2. Traitement et extraction des méta-données (réutilisation de RecipeMetadataParser)
        val ingredientCandidates2D = mutableListOf<OcrTextLine>()
        val reordered1DLines = mutableListOf<String>()

        for (line2D in lines2D) {
            val normalizedText = Ocr1Corrector.normalize(line2D.text, repairs)
            val metaResult = RecipeMetadataParser.extractMetadata(
                normalizedText, results, vocabulary, line2D.bitmap, line2D.bounds
            )
            val cleanText = metaResult.remainingLine.ifBlank { normalizedText }.trim()

            if (line2D.top < instructionTopLimit - 30) {
                if (!metaResult.isPureMetadata && cleanText.isNotBlank()) {
                    ingredientCandidates2D.add(line2D.copy(text = cleanText))
                }
            } else {
                if (cleanText.isNotBlank()) {
                    // Les instructions seront ré-assemblées après les ingrédients
                }
            }
        }

        // 3. Regroupement et Tri 2D des 2 colonnes d'ingrédients
        if (ingredientCandidates2D.isNotEmpty()) {
            val minLeft = ingredientCandidates2D.minOf { it.left }
            val rightColCandidate = ingredientCandidates2D.filter { it.left > minLeft + 200 }.minByOrNull { it.left }
            val splitX = if (rightColCandidate != null) (minLeft + rightColCandidate.left) / 2 else (minLeft + 350)

            val column1 = ingredientCandidates2D.filter { it.left < splitX }.sortedBy { it.top }
            val column2 = ingredientCandidates2D.filter { it.left >= splitX }.sortedBy { it.top }

            for (col1Line in column1) {
                reordered1DLines.add(col1Line.text.trim())
            }
            for (col2Line in column2) {
                reordered1DLines.add(col2Line.text.trim())
            }
        }

        // 4. Ajout des lignes d'instructions en fin de liste 1D
        val instructionLines2D = lines2D.filter { it.top >= instructionTopLimit - 30 }
        for (instLine in instructionLines2D.sortedBy { it.top }) {
            val cleanInst = Ocr1Corrector.normalize(instLine.text, repairs).trim()
            if (cleanInst.isNotBlank()) {
                reordered1DLines.add(cleanInst)
            }
        }

        LogManager.d(TAG, "Reorder2DLines: ${ingredientCandidates2D.size} ingrédients 2D ré-ordonnés en 1D.")
        return reordered1DLines
    }
}
