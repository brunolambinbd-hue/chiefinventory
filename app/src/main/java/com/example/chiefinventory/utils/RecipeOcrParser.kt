package com.example.chiefinventory.utils

import android.content.res.Resources
import android.util.Log
import com.google.mlkit.vision.text.Text

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
    fun parse(visionText: Text, vocabulary: OcrVocabulary): RecipeOcrResult {
        Log.d(TAG, "=== DÉBUT PIPELINE OCR (Extraction par blocs) ===")

        val rawLines = visionText.textBlocks.flatMap { block ->
            block.lines.map { it.text }
        }

        return executePipeline(rawLines, vocabulary)
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

        return executePipeline(rawLines, vocabulary)
    }

    /**
     * Exécute les 5 étapes séquentielles du pipeline OCR en utilisant la liste de lignes brutes [rawLines] et [vocabulary].
     *
     * @param rawLines La liste initiale des lignes de texte brutes extraites par l'OCR.
     * @param vocabulary Le vocabulaire métier data-driven contenant tous les dictionnaires XML.
     * @return L'objet [RecipeOcrResult] alimenté avec les données extraites et nettoyées.
     */
    private fun executePipeline(rawLines: List<String>, vocabulary: OcrVocabulary): RecipeOcrResult {

        // LOG [0] : TEXTE BRUT INITIAL
        Log.d(TAG, "[0] AVANT NORMALISATION: texte brut:\n${rawLines.joinToString("\n")}")

        val preparationModifiers = vocabulary.ingredientPreparationModifiers
        val commonIngredients = vocabulary.commonIngredientsNoQty
        val repairs = vocabulary.ocrSpellingRepairs

        // ÉTAPE 1 : Normalisation
        val normalizedLines = rawLines.map { Ocr1Normalizer.normalize(it, repairs) }
        Log.d(TAG, "[1] APRÈS NORMALISATION:\n${normalizedLines.joinToString("\n")}")

        // ÉTAPE 2 : Nettoyage (Suppression du bruit)
        val cleanedLines = Ocr2Cleaner.clean(normalizedLines, vocabulary.excludedOcrKeywords, vocabulary.adExclusions)
        Log.d(TAG, "[2] APRÈS NETTOYAGE:\n${cleanedLines.joinToString("\n")}")

        // ÉTAPE 2.5 : Extraction du titre et réparation des lettrines
        val titleResult = OcrTitleExtractor.extractTitle(cleanedLines, vocabulary)
        val linesWithoutTitle = titleResult.cleanedLines
        Log.d(TAG, "[2.5] TITRE DÉTECTÉ: ${titleResult.title ?: "Aucun"}")

        // ÉTAPE 3 : Catégorisation (Tri intelligent)
        val sections = Ocr3Categorizer.categorize(linesWithoutTitle, vocabulary)
        if (sections.detectedTitle == null) {
            sections.detectedTitle = titleResult.title
        }
        Log.d(TAG, "[3] CATÉGORISATION TERMINÉE")

        // LOG [3.1] : DÉTAIL DES INGRÉDIENTS APRÈS TRI
        Log.d(TAG, "[3.1] INVENTAIRE DES MOTS-CLÉS (seenIngredientKeywords): ${sections.ingredientKeywords}")
        Log.d(TAG, "[3.1] DÉCOMPOSITION DES INGRÉDIENTS BRUTS:")
        sections.rawIngredientsList.forEachIndexed { i, ing ->
            val parsed = IngredientParser.parse(ing)
            Log.d(TAG, "        RAW_ING[$i]: $ing -> Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}'")
        }

        // ÉTAPE 4 : Fusion (Merger)
        val mergedIngredients = Ocr4Merger.mergeIngredients(
            sections.rawIngredientsList, 
            preparationModifiers, 
            commonIngredients, 
            vocabulary.mandatoryComplementIngredients
        )
        val mergedInstructions = Ocr4Merger.mergeInstructions(sections.rawInstructionsList)

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
            vocabulary.culinaryMeasurementUnits
        )

        Log.d(TAG, "[4-5] RÉSULTATS FINAUX (BEAUTIFIED):")
        Log.d(TAG, "    TITRE FINAL: ${sections.detectedTitle}")
        finalIngredients.forEachIndexed { i, ing -> 
            Log.d(TAG, "    ING[$i]: $ing")
            val parsed = IngredientParser.parse(ing)
            Log.d(TAG, "    -> SPLIT FINAL: Q=${parsed.quantity}, U=${parsed.unit}, N='${parsed.name}'")
        }
        finalInstructions.forEachIndexed { i, inst -> Log.d(TAG, "    INSTR[$i]: $inst") }

        val result = RecipeOcrResult(
            title = sections.detectedTitle,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = finalInstructions.joinToString("\n"),
            wine = if (sections.detectedWineList.isNotEmpty()) sections.detectedWineList.joinToString(" ") else null,
            source = if (sections.detectedSourceList.isNotEmpty()) sections.detectedSourceList.joinToString(", ") else null,
            servings = sections.detectedServings,
            prepTime = sections.detectedPrepTime,
            cookTime = sections.detectedCookTime,
            restingTime = sections.detectedRestingTime,
            kcalPerServing = sections.detectedKcal,
            difficulty = sections.detectedDifficulty,
            nutritionInfo = sections.detectedNutritionInfo
        )

        // LOG FINAL DES MÉTA-DONNÉES EXTRAITES
        Log.d(TAG, "[6] MÉTA-DONNÉES EXTRAITES:")
        Log.d(TAG, "    - Portions: ${result.servings ?: "N/A"}")
        Log.d(TAG, "    - Kcal/por: ${result.kcalPerServing ?: "N/A"}")
        Log.d(TAG, "    - Difficulté: ${result.difficulty ?: "N/A"}")
        Log.d(TAG, "    - Prép (min): ${result.prepTime ?: "N/A"}")
        Log.d(TAG, "    - Cuisson (min): ${result.cookTime ?: "N/A"}")
        Log.d(TAG, "    - Repos (min): ${result.restingTime ?: "N/A"}")
        Log.d(TAG, "    - Vin: ${result.wine ?: "N/A"}")
        Log.d(TAG, "    - Source: ${result.source ?: "N/A"}")
        Log.d(TAG, "=== FIN PIPELINE OCR ===")

        return result
    }
}
