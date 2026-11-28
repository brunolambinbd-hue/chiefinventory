package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Aiguilleur intelligent de l'Étape 3 du pipeline OCR :
 * Évalue la structure de la fiche de recette et oriente les lignes nettoyées vers l'un des 4 Lecteurs de Recettes :
 * 1. [Ocr3CategorizerOneColumnIngredientFirst] (Type 1 : 1 colonne, ingrédients d'abord)
 * 2. [Ocr3CategorizerTwoColumnsIngredientFirst] (Type 2 : 2 colonnes d'ingrédients, positionnement 2D BoundingBox)
 * 3. [Ocr3CategorizerOneColumnOnlyInstructions] (Type 3 : 1 colonne, instructions seules)
 * 4. [Ocr3CategorizerOneColumnInstructionFirst] (Type 4 : 1 colonne, instructions en premier)
 */
object Ocr3CategorizerRouter {

    private const val TAG = "Ocr3CategorizerRouter"

    /**
     * Énumération représentative des 4 Lecteurs de Recettes supportés par le pipeline OCR Étape 3.
     */
    enum class ReaderType {
        /** Lecteur 1 : Recette en 1 colonne avec Ingrédients en premier. */
        ONE_COLUMN_INGREDIENT_FIRST,

        /** Lecteur 2 : Recette avec 2 colonnes d'Ingrédients en premier (Positionnement 2D BoundingBox). */
        TWO_COLUMNS_INGREDIENT_FIRST,

        /** Lecteur 3 : Recette en 1 colonne avec Instructions seules (Ingrédients implicites extraits). */
        ONE_COLUMN_ONLY_INSTRUCTIONS,

        /** Lecteur 4 : Recette en 1 colonne avec Instructions en premier (Ingrédients tout en bas). */
        ONE_COLUMN_INSTRUCTION_FIRST
    }

    /**
     * Analyse la typologie de la recette et route les lignes vers le lecteur de recettes approprié.
     *
     * @param linesWithoutTitle La liste des lignes nettoyées sans le titre.
     * @param lines2D La liste des lignes 2D scannées avec coordonnées BoundingBox.
     * @param cleanedLines La liste initiale des lignes nettoyées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return L'objet [RawSections] alimenté par le lecteur sélectionné.
     */
    fun routeAndCategorize(
        linesWithoutTitle: List<String>,
        lines2D: List<OcrTextLine>,
        cleanedLines: List<String>,
        vocabulary: OcrVocabulary
    ): RawSections {
        val (readerType, lines2DWithoutTitle) = evaluateReaderType(linesWithoutTitle, lines2D, vocabulary)

        val logName = when (readerType) {
            ReaderType.ONE_COLUMN_ONLY_INSTRUCTIONS -> "3️⃣ Ocr3CategorizerOneColumnOnlyInstructions (Type 3 : 1 Colonne - Instructions Seules)"
            ReaderType.ONE_COLUMN_INSTRUCTION_FIRST -> "4️⃣ Ocr3CategorizerOneColumnInstructionFirst (Type 4 : 1 Colonne - Instructions en Premier)"
            ReaderType.TWO_COLUMNS_INGREDIENT_FIRST -> "2️⃣ Ocr3CategorizerTwoColumnsIngredientFirst (Type 2 : 2 Colonnes d'Ingrédients en Premier 2D)"
            ReaderType.ONE_COLUMN_INGREDIENT_FIRST -> "1️⃣ Ocr3CategorizerOneColumnIngredientFirst (Type 1 : 1 Colonne - Ingrédients en Premier)"
        }
        LogManager.d(TAG, "[3] ➔ LECTEUR SÉLECTIONNÉ : $logName")

        return when (readerType) {
            ReaderType.ONE_COLUMN_ONLY_INSTRUCTIONS ->
                Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(linesWithoutTitle, vocabulary)

            ReaderType.ONE_COLUMN_INSTRUCTION_FIRST ->
                Ocr3CategorizerOneColumnInstructionFirst.categorizeInvertedRecipe(linesWithoutTitle, vocabulary, cleanedLines)

            ReaderType.TWO_COLUMNS_INGREDIENT_FIRST ->
                Ocr3CategorizerTwoColumnsIngredientFirst.categorize(lines2DWithoutTitle, vocabulary)

            ReaderType.ONE_COLUMN_INGREDIENT_FIRST ->
                Ocr3CategorizerOneColumnIngredientFirst.categorize(linesWithoutTitle, vocabulary)
        }
    }

    /**
     * Évalue de manière isolée et découpée le type de lecteur de recette parmi les 4 lecteurs disponibles.
     *
     * @param linesWithoutTitle La liste des lignes nettoyées sans le titre.
     * @param lines2D La liste des lignes 2D scannées avec coordonnées BoundingBox.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La paire contenant le [ReaderType] sélectionné et la liste filtrée des lignes 2D sans le titre.
     */
    internal fun evaluateReaderType(
        linesWithoutTitle: List<String>,
        lines2D: List<OcrTextLine>,
        vocabulary: OcrVocabulary
    ): Pair<ReaderType, List<OcrTextLine>> {
        val repairs = vocabulary.ocrSpellingRepairs

        val isOnlyInstructions = Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithoutTitle, vocabulary)
        if (isOnlyInstructions) return Pair(ReaderType.ONE_COLUMN_ONLY_INSTRUCTIONS, emptyList())

        val isInstructionFirst = Ocr3CategorizerOneColumnInstructionFirst.isInvertedRecipe(linesWithoutTitle, vocabulary)
        if (isInstructionFirst) return Pair(ReaderType.ONE_COLUMN_INSTRUCTION_FIRST, emptyList())

        val hasIngredientsHeader = vocabulary.ocr1ProtectedIngredientsHeaders.any { header ->
            val cleanHeader = header.trim().lowercase()
            linesWithoutTitle.any { line ->
                val lowerLine = line.trim().lowercase().removeSuffix(":").removeSuffix(".")
                lowerLine == cleanHeader || lowerLine.startsWith("$cleanHeader ") || lowerLine.startsWith("$cleanHeader:")
            }
        }

        val titleLines2D = lines2D.filter { line2D ->
            val cleanText = Ocr1Corrector.correct(line2D.text, repairs).trim().lowercase()
            isTitleCandidateLine(cleanText, linesWithoutTitle)
        }

        val maxBottomY = titleLines2D.maxOfOrNull { it.bottom }
        val titleBottomY = if (maxBottomY != null) maxBottomY else 0

        val lines2DWithoutTitleRaw = lines2D.map { line2D ->
            line2D.copy(text = Ocr1Corrector.normalize(line2D.text, repairs))
        }.filter { line2D ->
            line2D.top >= titleBottomY
        }

        val col1Lines = lines2DWithoutTitleRaw.filter { it.left < 450 }
        val otherLines = lines2DWithoutTitleRaw.filter { it.left >= 450 }
        val sortedCol1Lines = Ocr2DSpatialLayoutSorter.sortSpatially(col1Lines)
        val lines2DWithoutTitle = sortedCol1Lines + otherLines

        val isReal2DScan = lines2DWithoutTitle.any { is2DLine(it) }
        val isTwoColumns = !hasIngredientsHeader && isReal2DScan

        val selectedType = if (isTwoColumns) ReaderType.TWO_COLUMNS_INGREDIENT_FIRST else ReaderType.ONE_COLUMN_INGREDIENT_FIRST
        return Pair(selectedType, lines2DWithoutTitle)
    }

    private fun is2DLine(line: OcrTextLine): Boolean =
        (line.right > 300) or (line.left > 300) or (line.bottom > 300)

    /**
     * Détermine atomiquement si une ligne [cleanText] est un candidat titre exclu des [linesWithoutTitle].
     */
    internal fun isTitleCandidateLine(
        cleanText: String,
        linesWithoutTitle: List<String>
    ): Boolean {
        if (cleanText.isBlank()) return false
        return linesWithoutTitle.none { target ->
            val targetLower = target.trim().lowercase()
            if (cleanText == targetLower) {
                true
            } else if (targetLower.contains(cleanText)) {
                true
            } else {
                cleanText.contains(targetLower)
            }
        }
    }
}
