package com.chefinventory.app.utils

import android.content.res.Resources
import android.util.Log

/**
 * Étape 3 du pipeline OCR : Catégorisation intelligente et Machine à États (Orchestrateur Principal).
 *
 * Responsabilités :
 * - Répartir les lignes de texte nettoyées dans leurs sections respectives (Ingrédients, Instructions, Vin, Méta-données).
 * - Déléguer la sous-analyse séquentielle aux processeurs assistants numérotés [Ocr3Categorizer0_Context] à [Ocr3Categorizer5_BlockE_Dispatch].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer {
    private const val TAG = "Ocr3Categorizer"

    internal const val SECTION_NONE = 0
    internal const val SECTION_INGREDIENTS = 1
    internal const val SECTION_INSTRUCTIONS = 2

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant les ressources [Resources] Android.
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param res Les ressources de l'application Android permettant de charger [OcrVocabulary].
     * @return L'objet [RawSections] contenant les listes brutes catégorisées et méta-données.
     */
    fun categorize(lines: List<String>, res: Resources): RawSections {
        return categorize(lines, OcrVocabulary.fromResources(res))
    }

    /**
     * Classifie les lignes dans les différentes sections de la recette en utilisant le vocabulaire métier [OcrVocabulary] (TDD/Tests).
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return L'objet [RawSections] contenant les listes brutes catégorisées et méta-données.
     */
    fun categorize(lines: List<String>, vocabulary: OcrVocabulary): RawSections {
        val isNarrative = Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
        Log.d(TAG, "DÉTECTION TYPE RECETTE: isNarrativeInstructionOnlyRecipe = $isNarrative")
        if (isNarrative) {
            Log.d(TAG, "  => ACTIVER DÉLÉGATION NARRATIVE TYPE 2 (Ocr3NarrativeCategorizer)")
            return Ocr3NarrativeCategorizer.categorizeNarrativeRecipe(lines, vocabulary)
        }

        val results = RawSections()
        var currentSection = SECTION_NONE
        var isIngredientListClosed = false
        val wineState = Ocr3CategorizerWine.WineState()
        val seenIngredientKeywords = mutableSetOf<String>()

        Log.d(TAG, "--- DÉBUT CATÉGORISATION ---")

        // === DEBUT BLOC 0 : Dictionnaires & Compilations Regex ===
        val ctx = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)
        val hardQtyRegex = ctx.hardQtyRegex
        val qtyRegex = ctx.qtyRegex
        val containsActionRegex = ctx.containsActionRegex
        val ingredientConnectors = ctx.ingredientConnectors
        // === FIN BLOC 0 ===

        for (line in lines) {
            var workingLine = line.trim()
            val isEmptyWorkingLine = workingLine.isEmpty()
            if (isEmptyWorkingLine) {
                continue
            } else {
                Log.d(TAG, "  [LIGNE TRAITÉE] '$workingLine'")
            }

            // === DEBUT BLOC A : Méta-données & Continuation de phrase suspendue ===
            val blockA = Ocr3Categorizer1_BlockA_Metadata.processBlockA_MetadataAndHangingLine(
                workingLine, results, vocabulary, containsActionRegex,
                ingredientConnectors, currentSection, seenIngredientKeywords
            )
            if (blockA.shouldContinue) continue

            workingLine = blockA.workingLine
            var lowerLine = blockA.lowerLine
            var containsAction = blockA.containsAction
            var startsWithBullet = blockA.startsWithBullet
            val startsWithConnector = blockA.startsWithConnector
            // === FIN BLOC A ===

            // === DEBUT BLOC B : Ingrédient inachevé, Vin, Source & Purge Mots Exclus ===
            val blockB = Ocr3Categorizer2_BlockB_WineSource.processBlockB_UnfinishedWineAndExcludedKeywords(
                workingLine, currentSection, containsAction, startsWithBullet, startsWithConnector,
                isIngredientListClosed, vocabulary, results, seenIngredientKeywords, wineState
            )
            if (blockB.shouldContinue) continue
            workingLine = blockB.workingLine
            // === FIN BLOC B ===

            // === DEBUT BLOC C : Analyse Réapparition, Modificateurs & En-têtes ===
            val blockC = Ocr3Categorizer3_BlockC_Reappearance.processBlockC_ReappearanceAndHeaders(
                workingLine, currentSection, isIngredientListClosed, startsWithConnector,
                containsActionRegex, hardQtyRegex, qtyRegex, vocabulary, results, seenIngredientKeywords
            )
            lowerLine = blockC.lowerLine
            containsAction = blockC.containsAction
            startsWithBullet = blockC.startsWithBullet
            val hasWeight = blockC.hasWeight
            val hasHardQuantity = blockC.hasHardQuantity
            val isReappearance = blockC.isReappearance
            val hasInstructionTrigger = blockC.hasInstructionTrigger
            val looksLikeIngredient = blockC.looksLikeIngredient
            currentSection = blockC.updatedSection
            isIngredientListClosed = blockC.updatedIngredientListClosed
            // === FIN BLOC C ===

            // === DEBUT BLOC D : Bruit de pied de page, Signaux d'instructions & Classification de section ===
            val blockD = Ocr3Categorizer4_BlockD_Classification.processBlockD_SectionClassification(
                workingLine, currentSection, isIngredientListClosed, containsAction,
                startsWithBullet, startsWithConnector, hasWeight, hasHardQuantity,
                isReappearance, hasInstructionTrigger, looksLikeIngredient, wineState, vocabulary, results
            )
            if (blockD.shouldContinue) continue
            workingLine = blockD.workingLine
            currentSection = blockD.updatedSection
            // === FIN BLOC D ===

            // === DEBUT BLOC E : Aiguillage de section (Ingrédients, Instructions, Rattrapage) ===
            isIngredientListClosed = Ocr3Categorizer5_BlockE_Dispatch.processBlockE_SectionDispatch(
                workingLine, lowerLine, currentSection, isIngredientListClosed, containsAction,
                startsWithBullet, startsWithConnector, hasWeight, hasHardQuantity,
                isReappearance, hasInstructionTrigger, looksLikeIngredient,
                vocabulary, results, seenIngredientKeywords
            )
            // === FIN BLOC E ===
        }
        results.detectedWineList.addAll(wineState.detectedWineList)
        results.ingredientKeywords = seenIngredientKeywords
        Log.d(TAG, "--- FIN CATÉGORISATION ---")
        return results
    }

    /**
     * Extrait les mots-clés d'ingrédients pertinents à partir du nom nettoyé et de l'information supplémentaire.
     *
     * @param name Le nom principal de l'ingrédient.
     * @param supplementalInfo L'information supplémentaire associée.
     * @return La liste des mots-clés significatifs de longueur >= 3.
     */
    internal fun extractKeywordsFromText(name: String, supplementalInfo: String?): List<String> {
        val stopWords = setOf("dans", "avec", "pour", "plus", "moins", "vers", "sous", "sur", "chez", "entre", "faire", "faites", "votre", "quelques", "plusieurs")
        val fillers = setOf("morceau", "morceaux", "tranche", "tranches", "de", "le", "la", "les", "des", "du", "un", "une")
        val units = setOf("grammes", "kilos", "litres", "centilitres", "millilitres", "cuillere", "soupe", "cafe", "pincee", "gousse", "boite", "boites", "pot", "pots", "cl", "ml", "kg")
        
        val combinedText = if (supplementalInfo != null) "$name $supplementalInfo" else name
        return combinedText.lowercase().split(Regex("[\\s,.'’()\\-*•/0-9]+")).filter { it.length >= 3 && it !in stopWords && it !in units && it !in fillers }
    }

}
