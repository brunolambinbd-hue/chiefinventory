package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de l'Étape 3 (Analyseur Spatial 2D d'Axe X & Signature de Contenu) :
 * Isole et regroupe géométriquement les colonnes verticales indépendantes de la page d'après les coordonnées BoundingBox 2D
 * et calcule la Signature de Contenu XML pour chaque colonne.
 *
 * Responsabilités :
 * - Identifier la Colonne Gauche et la Colonne Droite.
 * - Évaluer la Signature de Contenu XML (> 60% d'ingrédients -> COLONNE INGRÉDIENTS, > 60% de verbes d'action -> COLONNE INSTRUCTIONS).
 * - Restituer un objet [SpatialTwoColumnsResult] contenant les lignes 2D de la Colonne Gauche et de la Colonne Droite triées par altitude Y.
 */
object Ocr3SpatialColumnClusteredGrouper {

    private const val TAG = "Ocr3SpatialColumnGrouper"

    /**
     * Type de contenu qualifié pour une colonne BoundingBox 2D.
     */
    enum class ColumnType {
        INGREDIENTS,
        INSTRUCTIONS,
        UNKNOWN
    }

    /**
     * Signature de Contenu XML calculée pour une colonne BoundingBox 2D.
     *
     * @property type Le type de colonne qualifié ([ColumnType.INGREDIENTS], [ColumnType.INSTRUCTIONS] ou [ColumnType.UNKNOWN]).
     * @property ingredientRatio Le ratio de lignes d'ingrédients (entre 0.0 et 1.0).
     * @property instructionRatio Le ratio de lignes d'instructions (entre 0.0 et 1.0).
     */
    data class ColumnSignature(
        val type: ColumnType,
        val ingredientRatio: Float,
        val instructionRatio: Float
    )

    /**
     * Résultat du regroupement spatial 2D des deux colonnes indépendantes.
     *
     * @property columnLeft La liste des lignes 2D appartenant à la Colonne Gauche, triées par altitude Y.
     * @property columnRight La liste des lignes 2D appartenant à la Colonne Droite, triées par altitude Y.
     * @property leftSignature La signature de contenu XML calculée pour la Colonne Gauche.
     * @property rightSignature La signature de contenu XML calculée pour la Colonne Droite.
     */
    data class SpatialTwoColumnsResult(
        val columnLeft: List<OcrTextLine>,
        val columnRight: List<OcrTextLine>,
        val leftSignature: ColumnSignature = ColumnSignature(ColumnType.UNKNOWN, 0f, 0f),
        val rightSignature: ColumnSignature = ColumnSignature(ColumnType.UNKNOWN, 0f, 0f)
    )

    /**
     * Calcule la Signature de Contenu XML d'une colonne BoundingBox 2D.
     *
     * @param columnLines La liste des lignes 2D appartenant à une colonne.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return L'objet [ColumnSignature] indiquant si la colonne est INGREDIENTS, INSTRUCTIONS ou UNKNOWN.
     */
    fun evaluateColumnSignature(
        columnLines: List<OcrTextLine>,
        vocabulary: OcrVocabulary
    ): ColumnSignature {
        if (columnLines.isEmpty()) {
            return ColumnSignature(ColumnType.UNKNOWN, 0f, 0f)
        }

        var ingredientCount = 0
        var instructionCount = 0

        for (line in columnLines) {
            val text = line.text.trim()
            val parsed = IngredientParser.parse(text, vocabulary.culinaryMeasurementUnits)
            val hasBullet = text.startsWith("•") || text.startsWith("-") || text.startsWith("*") || text.startsWith(".")
            val hasQty = parsed.quantity != null
            val hasUnit = parsed.unit != null
            val isIngr = hasQty || hasUnit || hasBullet

            if (isIngr) {
                ingredientCount++
            }

            val hasAction = OcrInstructionUtils.hasActionVerb(text, vocabulary.stepActionKeywords)
            val hasStartVerb = OcrInstructionUtils.hasStartVerb(text, vocabulary.stepStartVerbs)
            if (hasAction) {
                instructionCount++
            } else if (hasStartVerb) {
                instructionCount++
            }
        }

        val total = columnLines.size.toFloat()
        val ingredientRatio = ingredientCount / total
        val instructionRatio = instructionCount / total

        val type = when {
            ingredientRatio >= 0.60f -> ColumnType.INGREDIENTS
            instructionRatio >= 0.60f -> ColumnType.INSTRUCTIONS
            else -> ColumnType.UNKNOWN
        }

        LogManager.d(TAG, "EvaluateColumnSignature: Type=$type, ingrRatio=$ingredientRatio, instRatio=$instructionRatio")
        return ColumnSignature(type, ingredientRatio, instructionRatio)
    }

    /**
     * Regroupe spatialement les lignes 2D en deux colonnes verticales indépendantes et évalue leurs signatures de contenu XML.
     *
     * @param ingredientCandidates2D La liste des lignes 2D d'ingrédients candidates.
     * @param vocabulary Le vocabulaire métier OCR (facultatif pour calculer les signatures).
     * @return L'objet [SpatialTwoColumnsResult] contenant les deux colonnes triées verticalement et leurs signatures.
     */
    fun groupLinesByXColumns(
        ingredientCandidates2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary? = null
    ): SpatialTwoColumnsResult {
        if (ingredientCandidates2D.isEmpty()) {
            return SpatialTwoColumnsResult(emptyList(), emptyList())
        }

        val minLeft = ingredientCandidates2D.minOf { it.left }
        val rightColCandidate = ingredientCandidates2D.filter { it.left > minLeft + 200 }.minByOrNull { it.left }
        val splitX = if (rightColCandidate != null) {
            (minLeft + rightColCandidate.left) / 2
        } else {
            minLeft + 350
        }

        val columnLeft = ingredientCandidates2D.filter { it.left < splitX }.sortedBy { it.top }
        val columnRight = ingredientCandidates2D.filter { it.left >= splitX }.sortedBy { it.top }

        val leftSig = if (vocabulary != null) evaluateColumnSignature(columnLeft, vocabulary) else ColumnSignature(ColumnType.UNKNOWN, 0f, 0f)
        val rightSig = if (vocabulary != null) evaluateColumnSignature(columnRight, vocabulary) else ColumnSignature(ColumnType.UNKNOWN, 0f, 0f)

        LogManager.d(TAG, "GroupLinesByXColumns: splitX=$splitX, ColGauche=${columnLeft.size} (sig=${leftSig.type}), ColDroite=${columnRight.size} (sig=${rightSig.type}).")
        return SpatialTwoColumnsResult(columnLeft, columnRight, leftSig, rightSig)
    }

    /**
     * Étape 3 : Assemblage du Flux 1D Ré-ordonné Parfait.
     *
     * 1. Collecte toutes les BoundingBoxes de la Colonne Ingrédients triées du haut vers le bas.
     * 2. Collecte toutes les BoundingBoxes de la Colonne Instructions triées du haut vers le bas.
     * 3. Concatène les deux colonnes en un Flux 1D Ré-ordonné Parfait.
     *
     * @param twoColumnsResult Le résultat du regroupement spatial [SpatialTwoColumnsResult].
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La liste 1D de lignes ré-ordonnées prête pour [Ocr3CategorizerOneColumnIngredientFirst.categorize].
     */
    fun assembleReordered1DStream(
        twoColumnsResult: SpatialTwoColumnsResult,
        vocabulary: OcrVocabulary
    ): List<String> {
        val repairs = vocabulary.ocrSpellingRepairs
        val stream1D = mutableListOf<String>()

        // 1. Collecter toutes les BoundingBoxes de la Colonne Ingrédients (Gauche) triées du haut vers le bas
        val sortedIngredientsColLeft = twoColumnsResult.columnLeft.sortedBy { it.top }
        for (line in sortedIngredientsColLeft) {
            val normalizedText = Ocr1Corrector.normalize(line.text, repairs).trim()
            val cleanIngrText = normalizedText.replace(Regex("(?iu)^\\s*[•.*\\-]+\\s*"), "").trim()
            if (cleanIngrText.isNotBlank()) {
                stream1D.add(cleanIngrText)
            }
        }

        // 2. Collecter toutes les BoundingBoxes de la Colonne Ingrédients (Droite) triées du haut vers le bas
        val sortedIngredientsColRight = twoColumnsResult.columnRight.sortedBy { it.top }
        for (line in sortedIngredientsColRight) {
            val normalizedText = Ocr1Corrector.normalize(line.text, repairs).trim()
            val cleanIngrText = normalizedText.replace(Regex("(?iu)^\\s*[•.*\\-]+\\s*"), "").trim()
            if (cleanIngrText.isNotBlank()) {
                stream1D.add(cleanIngrText)
            }
        }

        LogManager.d(TAG, "AssembleReordered1DStream: Flux 1D parfait assemblé (${stream1D.size} lignes).")
        return stream1D
    }
}
