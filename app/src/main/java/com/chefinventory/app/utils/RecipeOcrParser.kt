package com.chefinventory.app.utils

import android.graphics.Bitmap
import android.content.res.Resources
import android.util.Log
import com.google.mlkit.vision.text.Text
import com.parabd.lib.traceability.utils.LogManager

/**
 * Façade orchestratrice principale du pipeline OCR de recettes culinaires.
 *
 * Responsabilités :
 * - Offrir les points d'entrée officiels de l'application pour traiter soit un objet vision [Text] de ML Kit,
 *   soit du texte brut scanné (tests unitaires et support legacy).
 * - Enchaîner séquentiellement les 5 étapes du pipeline OCR :
 *   1. Normalisation des caractères, symboles et diacritiques ([Ocr1Normalizer]).
 *   2. Nettoyage du bruit publicitaire ([Ocr2Cleaner]).
 *   2.5. Extraction et scoring du titre ([OcrTitleExtractor]).
 *   3. Catégorisation intelligente et machine à états ([Ocr3Categorizer]).
 *   4. Fusion structurelle des césures et continuations ([Ocr4Merger]).
 *   5. Beautification et présentation finale ([Ocr5Beautifier]).
 * - Reconstituer et retourner l'objet structuré final [RecipeOcrResult].
 */
object RecipeOcrParser {
    private const val TAG = "RecipeOCR"

    /**
     * Point d'entrée recommandé utilisant l'objet [Text] de ML Kit et les ressources Android [Resources].
     *
     * @param visionText L'objet de résultat de reconnaissance textuelle fourni par Google ML Kit.
     * @param res Les ressources de l'application Android permettant de charger [OcrVocabulary].
     * @return Le résultat d'analyse structuré [RecipeOcrResult].
     */
    fun parse(visionText: Text, res: Resources): RecipeOcrResult {
        return parse(visionText, OcrVocabulary.fromResources(res))
    }

    /**
     * Point d'entrée recommandé utilisant l'objet [Text] de ML Kit et le vocabulaire métier [OcrVocabulary].
     *
     * @param visionText L'objet de résultat de reconnaissance textuelle fourni par Google ML Kit.
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return Le résultat d'analyse structuré [RecipeOcrResult].
     */
    fun parse(visionText: Text, vocabulary: OcrVocabulary, bitmap: Bitmap? = null): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Extraction par blocs 2D) ===")

        val lines2D = mutableListOf<OcrTextLine>()
        visionText.textBlocks.forEach { block ->
            block.lines.forEach { line ->
                val box = line.boundingBox
                Log.d(TAG, "RAW SCAN 2D: '${line.text}' [top=${box?.top}, left=${box?.left}, bottom=${box?.bottom}, right=${box?.right}]")
                val top = box?.top ?: 0
                val left = box?.left ?: 0
                val bottom = box?.bottom ?: 0
                val right = box?.right ?: 0
                lines2D.add(OcrTextLine(text = line.text, top = top, left = left, bottom = bottom, right = right, bitmap = bitmap))
            }
        }

        val rawLines = lines2D.map { it.text }
        return executePipeline(rawLines, vocabulary, lines2D)
    }

    /**
     * Point d'entrée TDD d'isolation 2D utilisant une liste de [OcrTextLine] et le vocabulaire [OcrVocabulary].
     */
    fun parse(lines2D: List<OcrTextLine>, vocabulary: OcrVocabulary): RecipeOcrResult {
        val rawLines = lines2D.map { it.text }
        return executePipeline(rawLines, vocabulary, lines2D)
    }

    /**
     * Point d'entrée de secours utilisant une chaîne de texte brut et les ressources Android [Resources].
     *
     * @param fullText Le texte brut complet de la recette scannée.
     * @param res Les ressources de l'application Android permettant de charger [OcrVocabulary].
     * @return Le résultat d'analyse structuré [RecipeOcrResult].
     */
    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        return parse(fullText, OcrVocabulary.fromResources(res))
    }

    /**
     * Point d'entrée de secours utilisant une chaîne de texte brut et le vocabulaire métier [OcrVocabulary] (utilisé par les tests unitaires TDD).
     *
     * @param fullText Le texte brut complet de la recette scannée.
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return Le résultat d'analyse structuré [RecipeOcrResult].
     */
    fun parse(fullText: String, vocabulary: OcrVocabulary): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Texte brut) ===")

        if (fullText.isBlank()) return RecipeOcrResult()

        val rawLines = fullText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val lines2D = rawLines.mapIndexed { idx, text -> OcrTextLine(text = text, top = idx * 50) }
        return executePipeline(rawLines, vocabulary, lines2D)
    }

    private fun executePipeline(
        rawLines: List<String>,
        vocabulary: OcrVocabulary,
        lines2D: List<OcrTextLine> = emptyList()
    ): RecipeOcrResult {
        if (rawLines.isEmpty()) return RecipeOcrResult()

        // LOG [0.0] : TRACÉ DÉDIÉ COPIER-COLLER AU FORMAT KOTLIN LIST FOR TESTS
        logKotlinList("[0] RAW SCAN ML KIT", rawLines)

        val preparationModifiers = vocabulary.ingredientPreparationModifiers
        val commonIngredients = vocabulary.commonIngredientsNoQty
        val repairs = vocabulary.ocrSpellingRepairs

        // ÉTAPE 1 : Correction et Normalisation
        val normalizedLines = rawLines.map { Ocr1Corrector.normalize(it, repairs) }
        logKotlinList("[1] APRÈS NORMALISATION", normalizedLines)

        // ÉTAPE 2 : Nettoyage (Suppression du bruit de scan, pubs et mots exclus + capture précoce des sources)
        val cleaningResult = Ocr2Cleaner.cleanWithVocabulary(normalizedLines, vocabulary)
        val cleanedLines = cleaningResult.cleanedLines
        logKotlinList("[2] APRÈS NETTOYAGE", cleanedLines)

        // ÉTAPE 3 : Extraction du Titre et Catégorisation
        val titleResult = Ocr3CategorizerTitleExtractor.extractTitle(cleanedLines, vocabulary)
        val linesWithoutTitle = titleResult.cleanedLines
        Log.d(TAG, "[2.5] TITRE DÉTECTÉ: ${titleResult.title ?: "Aucun"}")

        // ÉTAPE 3 : Catégorisation (Aiguillage intelligent vers l'un des 4 Lecteurs de Recettes via Ocr3CategorizerRouter)
        val sections = Ocr3CategorizerRouter.routeAndCategorize(linesWithoutTitle, lines2D, cleanedLines, vocabulary)

        if (sections.detectedTitle == null) {
            sections.detectedTitle = titleResult.title
        }
        if (sections.detectedSourceList.isEmpty() && cleaningResult.earlyDetectedSources.isNotEmpty()) {
            sections.detectedSourceList.addAll(cleaningResult.earlyDetectedSources)
        }
        Log.d(TAG, "[3] CATÉGORISATION TERMINÉE")

        logKotlinList("[3] INGRÉDIENTS BRUTS CATÉGORISÉS", sections.rawIngredientsList)
        logKotlinList("[3] INSTRUCTIONS BRUTES CATÉGORISÉES", sections.rawInstructionsList)

        // ÉTAPE 4 : Fusion (Merger)
        val mergedIngredients = Ocr4Merger.mergeIngredients(
            rawIngredients = sections.rawIngredientsList, 
            preparationModifiers = preparationModifiers, 
            commonIngredients = commonIngredients, 
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            saltOrigins = vocabulary.saltVarietyOrigins,
            pepperOrigins = vocabulary.pepperDeOriginItems
        )
        val mergedInstructions = Ocr4Merger.mergeInstructions(sections.rawInstructionsList)

        logKotlinList("[4] INGRÉDIENTS FUSIONNÉS", mergedIngredients)
        logKotlinList("[4] INSTRUCTIONS FUSIONNÉES", mergedInstructions)

        // ÉTAPE 5 : Beautification (Présentation finale à l'écran)
        val finalIngredients = Ocr5Beautifier.beautifyIngredients(
            mergedIngredients,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )
        val finalInstructions = Ocr5Beautifier.beautifyInstructions(
            mergedInstructions, 
            vocabulary.stepActionKeywords, 
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits,
            vocabulary.ocrSpellingRepairs
        )
        val finalWines = Ocr5Beautifier.beautifyWines(sections.detectedWineList)
        val finalSources = Ocr5Beautifier.beautifySources(sections.detectedSourceList)

        logKotlinList("[5] INGRÉDIENTS FINAUX BEAUTIFIED", finalIngredients)
        logKotlinList("[5] INSTRUCTIONS FINALES BEAUTIFIED", finalInstructions)

        Log.d(TAG, "[4-5] RÉSULTATS FINAUX (BEAUTIFIED):")
        Log.d(TAG, "    TITRE FINAL: ${sections.detectedTitle}")
        finalIngredients.forEachIndexed { i, ing -> 
            Log.d(TAG, "    ING[$i]: $ing")
            val parsed = IngredientParser.parse(ing)
            Log.d(TAG, "    -> SPLIT FINAL: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}'")
        }
        finalInstructions.forEachIndexed { i, inst -> Log.d(TAG, "    INSTR[$i]: $inst") }

        val finalCategory = cleaningResult.earlyDetectedCategory ?: CategoryMapper.normalizeRecipeCategory(sections.detectedTitle)

        val finalTitle = Ocr5BeautifierTitle.beautifyTitle(sections.detectedTitle)

        val finalNutriScore = sections.detectedNutriScore?.trim()?.uppercase()?.takeIf {
            it in setOf("A", "B", "C", "D", "E")
        }

        val result = RecipeOcrResult(
            title = finalTitle,
            category = finalCategory,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = finalInstructions.joinToString("\n"),
            wine = if (finalWines.isNotEmpty()) finalWines.joinToString(" ") else null,
            source = if (finalSources.isNotEmpty()) finalSources.joinToString(", ") else null,
            servings = sections.detectedServings,
            prepTime = sections.detectedPrepTime,
            cookTime = sections.detectedCookTime,
            restingTime = sections.detectedRestingTime,
            kcalPerServing = sections.detectedKcal,
            difficulty = sections.detectedDifficulty,
            priceRating = sections.detectedPriceRating,
            nutritionInfo = sections.detectedNutritionInfo,
            nutriScore = finalNutriScore
        )

        // LOG FINAL DES MÉTA-DONNÉES EXTRAITES
        Log.d(TAG, "[6] MÉTA-DONNÉES EXTRAITES:")
        Log.d(TAG, "    - Portions: ${result.servings ?: "N/A"}")
        Log.d(TAG, "    - Kcal/por: ${result.kcalPerServing ?: "N/A"}")
        Log.d(TAG, "    - Difficulté: ${result.difficulty ?: "N/A"}")
        Log.d(TAG, "    - Prix/Coût: ${result.priceRating ?: "N/A"}")
        Log.d(TAG, "    - Prép (min): ${result.prepTime ?: "N/A"}")
        Log.d(TAG, "    - Cuisson (min): ${result.cookTime ?: "N/A"}")
        Log.d(TAG, "    - Repos (min): ${result.restingTime ?: "N/A"}")
        Log.d(TAG, "    - Vin: ${result.wine ?: "N/A"}")
        Log.d(TAG, "    - Source: ${result.source ?: "N/A"}")
        Log.d(TAG, "=== FIN PIPELINE OCR ===")

        return result
    }

    /**
     * Imprime une liste de chaînes dans Logcat sous la forme d'un tableau Kotlin copier-coller pour les tests unitaires.
     */
    private fun logKotlinList(phaseTitle: String, lines: List<String>) {
        Log.d(TAG, "=== DÉBUT $phaseTitle (FORMAT KOTLIN LIST) ===")
        lines.forEachIndexed { index, line ->
            val comma = if (index < lines.lastIndex) "," else ""
            Log.d(TAG, "\"$line\"$comma")
        }
        Log.d(TAG, "=== FIN $phaseTitle ===")
    }
}
