package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Lecteur 2 (Catégoriseur 2 Colonnes - Ingrédients en Premier) du pipeline OCR :
 * Traitement spatial 2D (BoundingBox) des fiches de recettes à 2 colonnes d'ingrédients en premier (ex: Soupe verte aux légumes).
 *
 * Responsabilités :
 * - Utiliser intensivement le positionnement BoundingBox 2D (`left`, `top`, `right`, `bottom`).
 * - Ré-ordonner la Colonne 1 (Gauche) puis la Colonne 2 (Droite).
 * - Déléguer 100% de la catégorisation d'ingrédients au moteur 1D [Ocr3CategorizerOneColumnIngredientFirst.categorize] (0 duplication).
 */
object Ocr3CategorizerTwoColumnsIngredientFirst {

    private const val TAG = "Ocr3CategorizerTwoColumnsIngredientFirst"

    /**
     * Catégorise une recette à 2 colonnes d'ingrédients en ré-ordonnant géométriquement les lignes 2D,
     * puis en déléguant la catégorisation d'ingrédients au moteur 1D [Ocr3CategorizerOneColumnIngredientFirst].
     *
     * @param lines2D La liste des lignes 2D scannées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return L'objet [RawSections] alimenté par le résultat ré-ordonné.
     */
    fun categorize(lines2D: List<OcrTextLine>, vocabulary: OcrVocabulary): RawSections {
        val results = RawSections()
        val instructionTopLimit = findInstructionTopLimit(lines2D, vocabulary, results)
        val candidates2D = extractIngredientCandidates(lines2D, instructionTopLimit, vocabulary, results)

        val twoColsResult = Ocr3SpatialColumnClusteredGrouper.groupLinesByXColumns(candidates2D, vocabulary)
        val reorderedIngredientsStream = Ocr3SpatialColumnClusteredGrouper.assembleReordered1DStream(twoColsResult, vocabulary)

        val instructionLines2D = lines2D.filter { it.top >= instructionTopLimit - 30 }
        val reorderedInstructions = extractInstructionLines(instructionLines2D, instructionTopLimit, vocabulary)

        val fullStream = reorderedIngredientsStream + reorderedInstructions
        return Ocr3CategorizerOneColumnIngredientFirst.categorizeDirect(fullStream, vocabulary)
    }

    /**
     * Reconstitue une liste 1D propre et ré-ordonnée (Colonne 1 Gauche, Colonne 2 Droite, Instructions)
     * en réutilisant les fonctions d'ingrédients et de méta-données existantes.
     *
     * @param lines2D La liste des lignes 2D nettoyées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param results L'objet [RawSections] alimenté avec les méta-données extraites.
     * @return La liste 1D de lignes ré-ordonnées prête pour [Ocr3CategorizerOneColumnIngredientFirst.categorize].
     */
    fun reorder2DLines(
        lines2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): List<String> {
        if (lines2D.isEmpty()) return emptyList()

        val instructionTopLimit = findInstructionTopLimit(lines2D, vocabulary, results)
        val ingredientCandidates2D = extractIngredientCandidates(lines2D, instructionTopLimit, vocabulary, results)

        val reordered1DLines = mutableListOf<String>()
        reordered1DLines.addAll(sortTwoColumnsSpatially(ingredientCandidates2D))
        reordered1DLines.addAll(extractInstructionLines(lines2D, instructionTopLimit, vocabulary))

        LogManager.d(TAG, "Reorder2DLines: ${ingredientCandidates2D.size} ingrédients 2D ré-ordonnés en 1D.")
        return reordered1DLines
    }

    /**
     * Pré-analyse dynamique de la position 2D de la première consigne d'instruction (0 seuil hardcodé).
     *
     * @param lines2D La liste des lignes 2D scannées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param results L'objet [RawSections] alimenté avec les méta-données.
     * @return La coordonnée Y 'top' de la première ligne d'instruction détectée, ou [Int.MAX_VALUE] si aucune.
     */
    internal fun findInstructionTopLimit(
        lines2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): Int {
        val actionKeywords = vocabulary.stepActionKeywords
        val startVerbs = vocabulary.stepStartVerbs
        val instructionHeaders = vocabulary.ocr1ProtectedInstructionsHeaders.map { it.lowercase().trim() }
        val repairs = vocabulary.ocrSpellingRepairs

        val firstInstructionLine = lines2D.firstOrNull { line ->
            val trimmed = Ocr1Corrector.normalize(line.text, repairs).trim()
            val cleanInstLine = OcrInstructionUtils.stripSingleLeadingAlphaOrBulletPrefix(trimmed)
            val lower = cleanInstLine.lowercase()
            val metaCheck = RecipeMetadataParser.extractMetadata(cleanInstLine, results, vocabulary, line.bitmap, line.bounds)
            val isMetaLine = metaCheck.isPureMetadata ||
                    lower.contains("cuisson") || lower.contains("préparation") || lower.contains("preparation") ||
                    OcrRegexes.Metadata.TIME_COMBINED.containsMatchIn(cleanInstLine) ||
                    OcrRegexes.Metadata.TIME_PREFIX.containsMatchIn(cleanInstLine) || OcrRegexes.Metadata.TIME_SUFFIX.containsMatchIn(cleanInstLine)
            if (isMetaLine) return@firstOrNull false

            val isInstructionHeader = instructionHeaders.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it:") }
            val hasStartVerb = OcrInstructionUtils.hasStartVerb(cleanInstLine, startVerbs)
            val hasActionVerb = OcrInstructionUtils.hasActionVerb(cleanInstLine, actionKeywords)
            isInstructionHeader || hasStartVerb || (hasActionVerb && cleanInstLine.length > 20)
        }

        return firstInstructionLine?.top ?: Int.MAX_VALUE
    }

    /**
     * Traitement et extraction des candidats d'ingrédients situés au-dessus de la limite d'instruction.
     *
     * @param lines2D La liste des lignes 2D scannées.
     * @param instructionTopLimit La limite d'altitude Y haute des instructions.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param results L'objet [RawSections] alimenté avec les méta-données.
     * @return La liste nettoyée des lignes d'ingrédients 2D candidates.
     */
    internal fun extractIngredientCandidates(
        lines2D: List<OcrTextLine>,
        instructionTopLimit: Int,
        vocabulary: OcrVocabulary,
        results: RawSections
    ): List<OcrTextLine> {
        val ingredientCandidates2D = mutableListOf<OcrTextLine>()
        val repairs = vocabulary.ocrSpellingRepairs

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
            }
        }
        return ingredientCandidates2D
    }

    /**
     * Regroupement et tri spatial 2D des 2 colonnes d'ingrédients (Colonne 1 Gauche puis Colonne 2 Droite).
     *
     * @param ingredientCandidates2D La liste des lignes d'ingrédients 2D candidates.
     * @return La liste 1D ordonnée des lignes d'ingrédients (Colonne 1 puis Colonne 2).
     */
    internal fun sortTwoColumnsSpatially(ingredientCandidates2D: List<OcrTextLine>): List<String> {
        return sortTwoColumnsSpatiallyByClusterAndSignature(ingredientCandidates2D)
    }

    /**
     * Regroupe et trie spatialement les colonnes d'ingrédients 2D en combinant le clustering X (Axe horizontal)
     * et la signature de contenu XML pour séparer déterministement la Colonne 1 (Gauche) et la Colonne 2 (Droite).
     *
     * @param ingredientCandidates2D La liste des lignes 2D d'ingrédients candidates.
     * @return La liste 1D ordonnée des lignes (Colonne 1 Gauche puis Colonne 2 Droite).
     */
    internal fun sortTwoColumnsSpatiallyByClusterAndSignature(
        ingredientCandidates2D: List<OcrTextLine>
    ): List<String> {
        if (ingredientCandidates2D.isEmpty()) return emptyList()

        val twoColumnsResult = Ocr3SpatialColumnClusteredGrouper.groupLinesByXColumns(ingredientCandidates2D)

        val reordered = mutableListOf<String>()
        for (col1Line in twoColumnsResult.columnLeft) {
            reordered.add(col1Line.text.trim())
        }
        for (col2Line in twoColumnsResult.columnRight) {
            reordered.add(col2Line.text.trim())
        }
        return reordered
    }

    /**
     * Collecte et nettoyage des lignes d'instructions situées au-dessous de la limite d'instruction.
     *
     * @param lines2D La liste des lignes 2D scannées.
     * @param instructionTopLimit La limite d'altitude Y haute des instructions.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La liste des consignes d'instructions nettoyées ordonnées par altitude.
     */
    internal fun extractInstructionLines(
        lines2D: List<OcrTextLine>,
        instructionTopLimit: Int,
        vocabulary: OcrVocabulary
    ): List<String> {
        val instructionLines = mutableListOf<String>()
        val repairs = vocabulary.ocrSpellingRepairs

        val instructionLines2D = lines2D.filter { it.top >= instructionTopLimit - 30 }
        val mainColInstLines = instructionLines2D.filter { it.left < 1200 }.sortedBy { it.top }
        val rightColInstLines = instructionLines2D.filter { it.left >= 1200 }.sortedBy { it.top }
        val sorted2DLines = mainColInstLines + rightColInstLines

        for (instLine in sorted2DLines) {
            val normalizedInst = Ocr1Corrector.normalize(instLine.text, repairs).trim()
            val cleanInst = OcrInstructionUtils.stripSingleLeadingAlphaOrBulletPrefix(normalizedInst)
            if (cleanInst.isNotBlank()) {
                instructionLines.add(cleanInst)
            }
        }
        return instructionLines
    }
}
