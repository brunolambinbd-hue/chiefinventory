package com.chefinventory.app.utils

/**
 * Conteneur de données du contexte de catégorisation préparé lors du Bloc 0 de l'Étape 3.
 */
internal data class CategorizationContext(
    val actionVerbs: List<String>,
    val commonIngredients: List<String>,
    val excludedKeywords: List<String>,
    val preparationModifiers: List<String>,
    val instructionTriggers: List<String>,
    val semanticExclusions: List<String>,
    val endMarkers: List<String>,
    val subHeaders: List<String>,
    val ingredientHeaders: List<String>,
    val instructionHeaders: List<String>,
    val hardQtyRegex: Regex,
    val qtyRegex: Regex,
    val containsActionRegex: Regex?,
    val ingredientConnectors: List<String>
)

/**
 * Sous-composant du Bloc 0 d'Étape 3 : Prépare et compile les dictionnaires métier, mots-clés et expressions régulières pour [Ocr3Categorizer].
 */
object Ocr3Categorizer0_Context {

    /**
     * Bloc 0 : Prépare et compile les dictionnaires métier, mots-clés et expressions régulières pour la catégorisation.
     *
     * @param vocabulary Le vocabulaire métier data-driven.
     * @return L'objet [CategorizationContext] contenant les regexes et dictionnaires compilés.
     */
    internal fun processBlock0_CategorizationContext(vocabulary: OcrVocabulary): CategorizationContext {
        val actionVerbs = vocabulary.stepActionKeywords.distinct()

        val ingrHeadersCombined = (vocabulary.ocr1ProtectedIngredientsHeaders + vocabulary.protectedSectionHeaders).distinct().map { it.lowercase() }
        val instrHeadersCombined = (vocabulary.ocr1ProtectedInstructionsHeaders + vocabulary.instructionSwitchKeywords + vocabulary.protectedSectionHeaders).distinct().map { it.lowercase() }
        val connectorsCombined = vocabulary.ingredientConnectors.distinct().map { it.lowercase() }

        return CategorizationContext(
            actionVerbs = actionVerbs,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            excludedKeywords = vocabulary.excludedOcrKeywords,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            instructionTriggers = vocabulary.instructionSwitchKeywords,
            semanticExclusions = vocabulary.ingredientSemanticExclusions,
            endMarkers = vocabulary.ingredientEndMarkers,
            subHeaders = vocabulary.ingredientSubHeaders,
            ingredientHeaders = ingrHeadersCombined,
            instructionHeaders = instrHeadersCombined,
            hardQtyRegex = Regex("^[\\d¼½¾]|[|Il!](?=[\\s\\d])|\\d+/\\d+", RegexOption.IGNORE_CASE),
            qtyRegex = Regex("^(?:[\\d\\-*•¼½¾]|un\\b|une\\b|des\\b|du\\b|de la\\b|de l'|le\\b|la\\b|les\\b|l['’]|quelques\\b|plusieurs\\b|un\\s+peu\\b)", RegexOption.IGNORE_CASE),
            containsActionRegex = if (actionVerbs.isEmpty()) null else Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b"),
            ingredientConnectors = connectorsCombined
        )
    }
}
