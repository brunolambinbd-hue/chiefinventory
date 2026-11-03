package com.chefinventory.app.utils

import android.util.Log
import java.util.regex.Pattern

/**
 * Résultat de l'analyse du Bloc C (Analyse Réapparition, Modificateurs & En-têtes).
 *
 * @property lowerLine La ligne nettoyée en minuscules.
 * @property containsAction Indique la présence d'un verbe d'action.
 * @property startsWithBullet Indique la présence d'une puce.
 * @property hasWeight Indique la présence d'un poids ou d'une masse explicite.
 * @property hasHardQuantity Indique la présence d'une quantité explicite forte.
 * @property isReappearance Indique si la ligne correspond à une réapparition d'ingrédient déjà vu.
 * @property hasInstructionTrigger Indique si la ligne contient un déclencheur d'instructions.
 * @property looksLikeIngredient Indique si la ligne ressemble à un ingrédient.
 * @property updatedSection Le nouvel état de section après analyse des en-têtes.
 * @property updatedIngredientListClosed L'état de verrouillage mis à jour.
 */
internal data class BlockCResult(
    val lowerLine: String,
    val containsAction: Boolean,
    val startsWithBullet: Boolean,
    val hasWeight: Boolean,
    val hasHardQuantity: Boolean,
    val isReappearance: Boolean,
    val hasInstructionTrigger: Boolean,
    val looksLikeIngredient: Boolean,
    val updatedSection: Int,
    val updatedIngredientListClosed: Boolean
)

/**
 * Sous-composant du Bloc C de l'Étape 3 : Analyse la réapparition d'ingrédients, identifie les modificateurs isolés et met à jour la section via les en-têtes.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3Categorizer3_BlockC_Reappearance {
    private const val TAG = "Ocr3Categorizer3_BlockC"

    internal fun evaluateIsReappearance(
        workingLine: String,
        isIngredientListClosed: Boolean,
        hasHardQuantity: Boolean,
        hasWeight: Boolean,
        startsWithConnector: Boolean,
        seenIngredientKeywords: Set<String>
    ): Boolean {
        if (startsWithConnector) return false
        Log.d(TAG, "LIGNE: '$workingLine' | Déjà inventorisés: $seenIngredientKeywords")
        return seenIngredientKeywords.any { keyword ->
            val pattern = Regex("(?<!\\p{L})${Regex.escape(keyword)}(?!\\p{L})", RegexOption.IGNORE_CASE)
            val match = pattern.containsMatchIn(workingLine)
            if (match) {
                val finalDecision = isIngredientListClosed || (!hasHardQuantity && !hasWeight)
                Log.d(TAG, "  ? Comparaison: '$keyword' -> MATCH=true | hardQty=$hasHardQuantity | weight=$hasWeight | closed=$isIngredientListClosed | final=$finalDecision")
            }
            val allowWithQty = isIngredientListClosed
            val noQty = !hasHardQuantity && !hasWeight
            match && (allowWithQty || noQty)
        }
    }

    internal fun isIsolatedModifier(lowerLine: String, modifiers: List<String>): Boolean {
        return modifiers.any { mod ->
            val lowerMod = mod.lowercase()
            if (lowerLine == lowerMod) {
                true
            } else if (lowerLine.startsWith("$lowerMod ")) {
                true
            } else if (lowerLine.startsWith("${lowerMod}d'")) {
                true
            } else if (lowerLine.startsWith("${lowerMod}de")) {
                true
            } else if (lowerLine.startsWith("${lowerMod}du")) {
                true
            } else {
                lowerLine.startsWith("${lowerMod}des")
            }
        }
    }

    /**
     * Bloc C : Analyse la réapparition d'ingrédients, identifie les modificateurs isolés et met à jour la section via les en-têtes.
     *
     * @param workingLine La ligne nettoyée.
     * @param currentSection La section courante.
     * @param isIngredientListClosed Indique si la liste d'ingrédients est verrouillée.
     * @param startsWithConnector Indique si la ligne commence par un connecteur.
     * @param containsActionRegex Regex de détection des verbes d'action.
     * @param hardQtyRegex Regex de détection de quantités fortes.
     * @param qtyRegex Regex de détection de quantités simples.
     * @param vocabulary Le vocabulaire métier.
     * @param results Le conteneur [RawSections] d'accumulation.
     * @param seenIngredientKeywords L'ensemble des mots-clés déjà vus.
     * @return L'objet [BlockCResult].
     */
    internal fun processBlockC_ReappearanceAndHeaders(
        workingLine: String,
        currentSection: Int,
        isIngredientListClosed: Boolean,
        startsWithConnector: Boolean,
        containsActionRegex: Regex?,
        hardQtyRegex: Regex,
        qtyRegex: Regex,
        vocabulary: OcrVocabulary,
        results: RawSections,
        seenIngredientKeywords: MutableSet<String>
    ): BlockCResult {
        val lowerLine = workingLine.lowercase()
        val containsAction = if (containsActionRegex != null) {
            containsActionRegex.containsMatchIn(workingLine)
        } else {
            false
        }

        val isBulletDot = workingLine.startsWith("•")
        val isBulletDash = workingLine.startsWith("-")
        val isBulletStar = workingLine.startsWith("*")
        val startsWithBullet = if (isBulletDot) {
            true
        } else if (isBulletDash) {
            true
        } else {
            isBulletStar
        }

        val weightRegex = Regex("(?i)\\(\\d+\\s*(?:g|kg|ml|cl|l|oz|lb|pcs|pce|un|une)\\)")
        val hasExplicitW = OcrIngredientUtils.hasExplicitWeight(workingLine)
        val hasWeight = if (hasExplicitW) {
            true
        } else {
            weightRegex.containsMatchIn(workingLine)
        }

        val hasHardQtyRegex = hardQtyRegex.containsMatchIn(workingLine)
        val hasHardQuantity = if (hasHardQtyRegex) {
            true
        } else {
            OcrHelperUtils.countIngredientSequences(workingLine) > 0
        }

        val hasQtyRegex = qtyRegex.containsMatchIn(workingLine)
        val hasQuantity = if (hasQtyRegex) {
            true
        } else {
            hasHardQuantity
        }

        // --- ANALYSE RÉAPPARITION AVEC LOGS DÉTAILLÉS ---
        val isReappearance = evaluateIsReappearance(
            workingLine, isIngredientListClosed, hasHardQuantity, hasWeight, startsWithConnector, seenIngredientKeywords
        )

        val isolatedMod = isIsolatedModifier(lowerLine, vocabulary.ingredientPreparationModifiers)

        val hasInstructionTrigger = vocabulary.instructionSwitchKeywords.any { Regex("(?i)\\b${Pattern.quote(it)}\\b").containsMatchIn(workingLine) }
        val isSubHeader = vocabulary.ingredientSubHeaders.any { it.equals(workingLine, ignoreCase = true) }

        val isCommonIngrStart = vocabulary.commonIngredientsNoQty.any { lowerLine.startsWith(it.lowercase()) }
        val looksLikeIngredient = (hasQuantity || hasWeight || isolatedMod || isCommonIngrStart) && !hasInstructionTrigger && !isReappearance

        val instructionHeaders = listOf("préparation", "preparation", "instructions", "nstructions", "étapes", "réalisation", "méthode", "progression", "cuisson")
        val matchesInstrList = instructionHeaders.any { lowerLine.contains(it) }
        val matchesInstrTrig = vocabulary.instructionSwitchKeywords.any { lowerLine == it.lowercase() }
        val isInstrHeader = (matchesInstrList || matchesInstrTrig) && workingLine.length < 35 && !containsAction

        val matchesIngrWord = lowerLine.contains("ingrédients") || lowerLine.contains("composition")
        val isIngrHeader = matchesIngrWord && workingLine.length < 35 && !containsAction

        var updatedSection = currentSection
        var updatedClosed = isIngredientListClosed

        when {
            isInstrHeader -> updatedSection = Ocr3Categorizer.SECTION_INSTRUCTIONS
            isIngrHeader || isSubHeader -> {
                updatedSection = Ocr3Categorizer.SECTION_INGREDIENTS
                updatedClosed = false
                if (isIngrHeader) {
                    seenIngredientKeywords.clear()
                    val hasRealInstructionStep = results.rawInstructionsList.any { OcrInstructionUtils.hasActionVerb(it, vocabulary.stepActionKeywords) }
                    if (!hasRealInstructionStep) {
                        results.rawInstructionsList.clear()
                    }
                }
            }
        }

        return BlockCResult(
            lowerLine = lowerLine,
            containsAction = containsAction,
            startsWithBullet = startsWithBullet,
            hasWeight = hasWeight,
            hasHardQuantity = hasHardQuantity,
            isReappearance = isReappearance,
            hasInstructionTrigger = hasInstructionTrigger,
            looksLikeIngredient = looksLikeIngredient,
            updatedSection = updatedSection,
            updatedIngredientListClosed = updatedClosed
        )
    }
}
