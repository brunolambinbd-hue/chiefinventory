package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R
import java.io.File

/**
 * Modèle de domaine représentant l'ensemble du vocabulaire, des mots-clés et des règles du pipeline OCR.
 *
 * Cette classe est une structure de données Kotlin pure, indépendante du Framework Android,
 * permettant au pipeline OCR d'être rapide, déterministe et testable sans mocks.
 *
 * @property stepActionKeywords Verbes d'action culinaires de consignes d'instructions.
 * @property commonIngredientsNoQty Ingrédients courants sans quantité (assaisonnements).
 * @property excludedOcrKeywords Mots-clés d'exclusion OCR.
 * @property ingredientPreparationModifiers Modificateurs de préparation d'ingrédients.
 * @property ingredientPreparationSuffixModifiers Modificateurs de préparation de fin d'élément.
 * @property instructionSwitchKeywords Mots-clés de bascule vers la section d'instructions.
 * @property ingredientSemanticExclusions Exclusions sémantiques d'ingrédients.
 * @property ingredientEndMarkers Marqueurs de fin de section d'ingrédients.
 * @property ingredientSubHeaders Sous-en-têtes de sous-sections d'ingrédients.
 * @property sourceKeywords Mots-clés de sources et provenances.
 * @property knownSourceBrands Enseignes et marques connues.
 * @property phonePrefixes Préfixes de numéros de téléphone.
 * @property wineAppellations Appellations de vins.
 * @property wineProducers Producteurs et châteaux de vins.
 * @property wineKeywords Mots-clés généraux d'œnologie.
 * @property wineTitleKeywords Mots-clés de titres de vins.
 * @property ocr3ExplicitWineHeaders En-têtes explicites d'accords mets et vins.
 * @property wineRemovePattern Motif Regex de nettoyage des lignes de vins.
 * @property ocrSpellingRepairs Table de réparation des erreurs d'orthographe OCR.
 * @property adExclusions Mots-clés d'exclusion publicitaires.
 * @property ocrNoiseExclusions Exclusions du bruit de numérisation OCR.
 * @property titleKeywords Mots-clés d'évaluation des titres de recettes.
 * @property mandatoryComplementIngredients Ingrédients de complément obligatoires.
 * @property hangingConnectors Connecteurs de liaison suspendus.
 * @property ingredientVarietyQualifiers Qualificatifs de variétés d'ingrédients.
 * @property baseIngredientsExpectingVariety Ingrédients de base attendant une variété.
 * @property standaloneArticles Articles isolés à nettoyer.
 * @property culinaryMeasurementUnits Unités de mesure culinaires et abréviations.
 * @property titleSpellingRepairs Table de réparation orthographique des titres.
 * @property titleMultilineConnectors Connecteurs de titres multi-lignes.
 * @property ingredientSplitterConnectors Connecteurs de scission multi-ingrédients.
 * @property ocrDiacriticRepairs Table de conversion des diacritiques et accents Unicode.
 * @property priceTitleKeywords Mots-clés d'en-tête de prix.
 * @property priceCategoriesLow Plages de prix bas.
 * @property priceCategoriesMedium Plages de prix moyens.
 * @property priceCategoriesHigh Plages de prix élevés.
 * @property ingredientConnectors Connecteurs de liaison d'ingrédients.
 * @property protectedSectionHeaders En-têtes de sections protégés.
 * @property timeHeaderPrefixes Préfixes d'en-têtes de temps de préparation.
 * @property timeHeaderKeywords Mots-clés d'en-têtes de temps de préparation/cuisson.
 * @property ocr1ProtectedIngredientsHeaders En-têtes protégés et variantes de la section Ingrédients (Étape 1).
 * @property ocr1ProtectedInstructionsHeaders En-têtes protégés et variantes de la section Instructions (Étape 1).
 * @property ocr2TechnicalDimensionKeywords Mots-clés de contexte de dimensions techniques (Étape 2).
 * @property orphanNounNumberWords Mots numériques et nombres exprimés en toutes lettres disqualifiant les noms orphelins.
 * @property orphanNounStopWords Mots-morts et bruits sémantiques exclus pour le complétement des noms orphelins.
 * @property narrativeStopWords Mots-morts exclus pour l'extraction d'ingrédients implicites des recettes narratives.
 * @property durationUnits Unités et abréviations de durée temporelle.
 * @property durationApproximationAdverbs Adverbes et qualificatifs d'approximation de quantité et durée.
 * @property stepStartVerbs Verbes d'action au mode impératif marquant le début d'une nouvelle étape.
 * @property stepSecondaryVerbs Verbes d'action secondaires en forme infinitive ou interne.
 * @property stepContextWords Mots et expressions de contexte d'instructions.
 * @property ocr1CanonicalIngredientsHeader Cible canonique de remplacement de l'en-tête d'ingrédients (Étape 1).
 * @property ocr1CanonicalInstructionsHeader Cible canonique de remplacement de l'en-tête d'instructions (Étape 1).
 * @property ocr1IngredientsSuffixPrefix Préfixe de détection du suffixe ingrédients (Étape 1).
 * @property logOcr1ProtectedHeaderRepaired Message de log pour la réparation des en-têtes d'étape 1.
 * @property logOcr1ProtectedIngredientsEmpty Message de log lorsque ocr1ProtectedIngredientsHeaders est vide.
 * @property logOcr1ProtectedInstructionsEmpty Message de log lorsque ocr1ProtectedInstructionsHeaders est vide.
 * @property logOcr2CleanerSourceCaptured Message de log pour la capture précoce de source dans Ocr2Cleaner.
 * @property logOcr2CleanerNoiseRemoved Message de log pour la suppression de bruit dans Ocr2Cleaner.
 * @property logOcr2CleanerAdLineRemoved Message de log pour la suppression de ligne publicitaire dans Ocr2Cleaner.
 * @property logOcr2CleanerAdPrefixRemoved Message de log pour la suppression de préfixe publicitaire dans Ocr2Cleaner.
 * @property logOcr2CleanerExcludedKeywordRemoved Message de log pour la suppression de mot-clé exclu dans Ocr2Cleaner.
 * @property logOcr3CategorizerNarrativeDelegation Message de log pour la délégation narrative d'étape 3.
 * @property logOcr3CategorizerStart Message de log pour le début de catégorisation d'étape 3.
 * @property logOcr3CategorizerEnd Message de log pour la fin de catégorisation d'étape 3.
 * @property logOcr3BlockAMetadataIgnored Message de log pour les métadonnées ignorées d'étape 3.
 * @property logOcr3BlockAFusionContinuation Message de log pour la fusion de continuation suspendue d'étape 3.
 * @property logOcr3BlockCReappearanceCheck Message de log pour la vérification de réapparition d'étape 3.
 * @property logOcr3BlockCReappearanceMatch Message de log pour le match de réapparition d'étape 3.
 * @property logOcr3BlockDClassificationSummary Message de log pour le bilan de classification d'étape 3.
 * @property logOcr3BlockEDispatchIngrSplit Message de log pour le split d'ingrédient d'étape 3.
 * @property logOcr3BlockEDispatchInstrAdded Message de log pour l'ajout d'instruction d'étape 3.
 * @property logOcr3BlockEDispatchCatchup Message de log pour le rattrapage d'ingrédient d'étape 3.
 * @property logOcr3BlockEDispatchLockActivated Message de log pour le verrou d'étape 3.
 * @property logOcr3HeaderSwitchInstructions Message de log pour le basculement vers instructions.
 * @property logOcr3HeaderSwitchIngredients Message de log pour le basculement vers ingrédients.
 * @property logOcr3HeaderSubheaderDetected Message de log pour la détection de sous-en-tête.
 * @property logOcr3UnfinishedIngrStillEmpty Message de log pour l'ingrédient inachevé toujours vide.
 * @property logOcr3UnfinishedIngrFused Message de log pour la fusion d'ingrédient inachevé.
 * @property logOcr3InvertedIsInverted Message de log pour la détection de recette inversée.
 * @property logOcr3InvertedStart Message de log pour le début de catégorisation inversée.
 * @property logOcr3InvertedEnd Message de log pour la fin de catégorisation inversée.
 * @property logOcr3InvertedBottomIngrSwitch Message de log pour la bascule en ingrédients du bas.
 * @property logOcr3NarrativeIsNarrative Message de log pour la détection de recette narrative.
 * @property logOcr3NarrativeStart Message de log pour le début de catégorisation narrative.
 * @property logOcr3NarrativeEnd Message de log pour la fin de catégorisation narrative.
 * @property logOcr3NarrativeExtractedImpliedIngr Message de log pour un ingrédient implicite extrait.
 * @property logOcr3NarrativeExtractedSeasoning Message de log pour un assaisonnement implicite extrait.
 * @property logOcr3NarrativeSeasoningAlreadyPresent Message de log lorsqu'un assaisonnement est déjà présent.
 * @property logOcr3TitleExtractorStart Message de log pour le début d'extraction de titre d'étape 3.
 * @property logOcr3TitleExtractorEnd Message de log pour la fin d'extraction de titre d'étape 3.
 * @property logOcr3TitleExtractorRetained Message de log pour le titre retenu d'étape 3.
 * @property logOcr3TitleExtractorIngrDetected Message de log pour les ingrédients détectés pour scoring.
 * @property logOcr3TitleExtractorNoIngr Message de log lorsqu'aucun ingrédient n'est détecté pour scoring.
 * @property logOcr3TitleExtractorLineProcessed Message de log pour les lignes traitées de titre.
 * @property logOcr3TitleExtractorCandidateScore Message de log pour les candidats titres scorés.
 * @property logOcr3TitleExtractorNoTitleRetained Message de log lorsqu'aucun titre valide n'est retenu.
 * @property logOcr3TitleExtractorRemainingLine Message de log pour les lignes restantes candidates.
 * @property logOcr3TitleExtractorSelPoivreCandidate Message de log pour le candidat titre extrait depuis sel/poivre.
 * @property logOcr3TitleRepairRuleInvalid Message de log pour une règle de réparation de titre invalide.
 * @property logOcr3TitleLettrineRepaired Message de log pour la réparation de lettrine du titre.
 * @property logOcr3TitleOrphanNounCompleted Message de log pour le titre complété par un nom orphelin.
 * @property logOcr3TitleNoOrphanNounFound Message de log lorsqu'aucun nom orphelin trouvé dans les lignes restantes.
 * @property logOcr3TitleStripRecipeNumberEmpty Message de log pour une chaîne vide après suppression du numéro de fiche.
 * @property logOcr3WineDuplicateIgnored Message de log pour la ligne de vin ignorée d'étape 3.
 * @property logOcr3WineEmptyIgnored Message de log pour la ligne de vin vide ignorée d'étape 3.
 * @property logOcr3WineSectionClosed Message de log pour la fermeture de section vin d'étape 3.
 * @property logOcr3WineSectionActive Message de log pour le maintien de section vin d'étape 3.
 * @property logOcr3WineAlreadyClosedIgnored Message de log pour la ligne de vin ignorée sur section fermée d'étape 3.
 * @property logOcr3WineImplicitVolumeClosed Message de log pour la fermeture de section vin implicite d'étape 3.
 * @property logOcr3WineImplicitNoVolumeOpen Message de log pour le vin implicite sans volume d'étape 3.
 * @property logOcr3WineImplicitDuplicateIgnored Message de log pour la ligne de vin implicite ignorée d'étape 3.
 * @property logOcr3WineImplicitEmptyIgnored Message de log pour la ligne de vin implicite vide ignorée d'étape 3.
 * @property logOcr4SplitterDebugMatch Message de log pour le match de découpage d'étape 4.
 * @property logOcr5BeautifierInstructionMerged Message de log pour la fusion d'instructions d'étape 5.
 * @property logOcr5BeautifierVarietyMerged Message de log pour la fusion de variété d'étape 5.
 * @property logOcr5BeautifierWineFormatted Message de log pour le formatage de vin d'étape 5.
 * @property logOcr5BeautifierSourceFormatted Message de log pour le formatage de source d'étape 5.
 * @property logOcrVocabLoaderCustomDirNoCache Message de log pour les répertoires personnalisés non mis en cache.
 * @property logErrDiacriticRepairs Message de log d'erreur de chargement des diacritiques.
 *
 * @see OcrVocabularyLoader
 */
data class OcrVocabulary(
    val stepActionKeywords: List<String> = emptyList(),
    val commonIngredientsNoQty: List<String> = emptyList(),
    val excludedOcrKeywords: List<String> = emptyList(),
    val ingredientPreparationModifiers: List<String> = emptyList(),
    val ingredientPreparationSuffixModifiers: List<String> = emptyList(),
    val instructionSwitchKeywords: List<String> = emptyList(),
    val ingredientSemanticExclusions: List<String> = emptyList(),
    val ingredientEndMarkers: List<String> = emptyList(),
    val ingredientSubHeaders: List<String> = emptyList(),
    val sourceKeywords: List<String> = emptyList(),
    val knownSourceBrands: List<String> = emptyList(),
    val phonePrefixes: List<String> = emptyList(),
    val wineAppellations: List<String> = emptyList(),
    val wineProducers: List<String> = emptyList(),
    val wineKeywords: List<String> = emptyList(),
    val wineTitleKeywords: List<String> = emptyList(),
    val ocr3ExplicitWineHeaders: List<String> = emptyList(),
    val wineRemovePattern: String = "",
    val ocrSpellingRepairs: List<String> = emptyList(),
    val adExclusions: List<String> = emptyList(),
    val recipeCategoryHeaders: List<String> = emptyList(),
    val ocrNoiseExclusions: List<String> = emptyList(),
    val titleKeywords: List<String> = emptyList(),
    val mandatoryComplementIngredients: List<String> = emptyList(),
    val hangingConnectors: List<String> = emptyList(),
    val ingredientVarietyQualifiers: List<String> = emptyList(),
    val baseIngredientsExpectingVariety: List<String> = emptyList(),
    val standaloneArticles: List<String> = emptyList(),
    val culinaryMeasurementUnits: List<String> = emptyList(),
    val titleSpellingRepairs: List<String> = emptyList(),
    val titleMultilineConnectors: List<String> = emptyList(),
    val ingredientSplitterConnectors: List<String> = emptyList(),
    val ocrDiacriticRepairs: List<String> = emptyList(),
    val priceTitleKeywords: List<String> = emptyList(),
    val priceCategoriesLow: List<String> = emptyList(),
    val priceCategoriesMedium: List<String> = emptyList(),
    val priceCategoriesHigh: List<String> = emptyList(),
    val ingredientConnectors: List<String> = emptyList(),
    val protectedSectionHeaders: List<String> = emptyList(),
    val timeHeaderPrefixes: List<String> = emptyList(),
    val timeHeaderKeywords: List<String> = emptyList(),
    val ocr1ProtectedIngredientsHeaders: List<String> = emptyList(),
    val ocr1ProtectedInstructionsHeaders: List<String> = emptyList(),
    val ocrSentenceConjunctions: List<String> = emptyList(),
    val sentenceEndingPunctuation: List<String> = emptyList(),
    val ocr2TechnicalDimensionKeywords: List<String> = emptyList(),
    val genericWineWords: List<String> = emptyList(),
    val orphanNounNumberWords: List<String> = emptyList(),
    val orphanNounStopWords: List<String> = emptyList(),
    val narrativeStopWords: List<String> = emptyList(),
    val durationUnits: List<String> = emptyList(),
    val durationApproximationAdverbs: List<String> = emptyList(),
    val stepStartVerbs: List<String> = emptyList(),
    val stepSecondaryVerbs: List<String> = emptyList(),
    val stepContextWords: List<String> = emptyList(),
    val ocr1CanonicalIngredientsHeader: String = "",
    val ocr1CanonicalInstructionsHeader: String = "",
    val ocr1IngredientsSuffixPrefix: String = "",
    val logOcr1ProtectedHeaderRepaired: String = "",
    val logOcr1ProtectedIngredientsEmpty: String = "",
    val logOcr1ProtectedInstructionsEmpty: String = "",
    val logOcr2CleanerSourceCaptured: String = "",
    val logOcr2CleanerNoiseRemoved: String = "",
    val logOcr2CleanerAdLineRemoved: String = "",
    val logOcr2CleanerAdPrefixRemoved: String = "",
    val logOcr2CleanerExcludedKeywordRemoved: String = "",
    val logOcr3CategorizerNarrativeDelegation: String = "",
    val logOcr3CategorizerStart: String = "",
    val logOcr3CategorizerEnd: String = "",
    val logOcr3BlockAMetadataIgnored: String = "",
    val logOcr3BlockAFusionContinuation: String = "",
    val logOcr3BlockCReappearanceCheck: String = "",
    val logOcr3BlockCReappearanceMatch: String = "",
    val logOcr3BlockDClassificationSummary: String = "",
    val logOcr3BlockEDispatchIngrSplit: String = "",
    val logOcr3BlockEDispatchInstrAdded: String = "",
    val logOcr3BlockEDispatchCatchup: String = "",
    val logOcr3BlockEDispatchLockActivated: String = "",
    val logOcr3HeaderSwitchInstructions: String = "",
    val logOcr3HeaderSwitchIngredients: String = "",
    val logOcr3HeaderSubheaderDetected: String = "",
    val logOcr3UnfinishedIngrStillEmpty: String = "",
    val logOcr3UnfinishedIngrFused: String = "",
    val logOcr3InvertedIsInverted: String = "",
    val logOcr3InvertedStart: String = "",
    val logOcr3InvertedEnd: String = "",
    val logOcr3InvertedBottomIngrSwitch: String = "",
    val logOcr3NarrativeIsNarrative: String = "",
    val logOcr3NarrativeStart: String = "",
    val logOcr3NarrativeEnd: String = "",
    val logOcr3NarrativeExtractedImpliedIngr: String = "",
    val logOcr3NarrativeExtractedSeasoning: String = "",
    val logOcr3NarrativeSeasoningAlreadyPresent: String = "",
    val logOcr3TitleExtractorStart: String = "",
    val logOcr3TitleExtractorEnd: String = "",
    val logOcr3TitleExtractorRetained: String = "",
    val logOcr3TitleExtractorIngrDetected: String = "",
    val logOcr3TitleExtractorNoIngr: String = "",
    val logOcr3TitleExtractorLineProcessed: String = "",
    val logOcr3TitleExtractorCandidateScore: String = "",
    val logOcr3TitleExtractorNoTitleRetained: String = "",
    val logOcr3TitleExtractorRemainingLine: String = "",
    val logOcr3TitleExtractorSelPoivreCandidate: String = "",
    val logOcr3TitleRepairRuleInvalid: String = "",
    val logOcr3TitleLettrineRepaired: String = "",
    val logOcr3TitleOrphanNounCompleted: String = "",
    val logOcr3TitleNoOrphanNounFound: String = "",
    val logOcr3TitleStripRecipeNumberEmpty: String = "",
    val logOcr3WineDuplicateIgnored: String = "",
    val logOcr3WineEmptyIgnored: String = "",
    val logOcr3WineSectionClosed: String = "",
    val logOcr3WineSectionActive: String = "",
    val logOcr3WineAlreadyClosedIgnored: String = "",
    val logOcr3WineImplicitVolumeClosed: String = "",
    val logOcr3WineImplicitNoVolumeOpen: String = "",
    val logOcr3WineImplicitDuplicateIgnored: String = "",
    val logOcr3WineImplicitEmptyIgnored: String = "",
    val logOcr4SplitterDebugMatch: String = "",
    val logOcr5BeautifierInstructionMerged: String = "",
    val logOcr5BeautifierVarietyMerged: String = "",
    val logOcr5BeautifierWineFormatted: String = "",
    val logOcr5BeautifierSourceFormatted: String = "",
    val logOcr0RawCopyPasteStart: String = "",
    val logOcr0RawCopyPasteEnd: String = "",
    val logOcrVocabLoaderCustomDirNoCache: String = "",
    val logErrDiacriticRepairs: String = ""
) {
    companion object {

        /**
         * Construit l'objet [OcrVocabulary] depuis les ressources Android d'une instance [Resources].
         *
         * @param res Les ressources Android de l'application.
         * @return L'objet [OcrVocabulary] alimenté.
         */
        fun fromResources(res: Resources): OcrVocabulary {
            val vocab = OcrVocabulary(
                stepActionKeywords = getStringArraySafely(res, R.array.step_start_verbs) + getStringArraySafely(res, R.array.step_secondary_verbs) + getStringArraySafely(res, R.array.step_context_words) + getStringArraySafely(res, R.array.step_action_keywords_complement),
                commonIngredientsNoQty = getStringArraySafely(res, R.array.common_ingredients_no_qty),
                excludedOcrKeywords = getStringArraySafely(res, R.array.excluded_ocr_keywords),
                ingredientPreparationModifiers = getStringArraySafely(res, R.array.ingredient_preparation_modifiers),
                ingredientPreparationSuffixModifiers = getStringArraySafely(res, R.array.ingredient_preparation_suffix_modifiers),
                instructionSwitchKeywords = getStringArraySafely(res, R.array.instruction_switch_keywords),
                ingredientSemanticExclusions = getStringArraySafely(res, R.array.ingredient_semantic_exclusions),
                ingredientEndMarkers = getStringArraySafely(res, R.array.ingredient_end_markers),
                ingredientSubHeaders = getStringArraySafely(res, R.array.ingredient_sub_headers),
                sourceKeywords = getStringArraySafely(res, R.array.source_keywords),
                knownSourceBrands = getStringArraySafely(res, R.array.known_source_brands),
                phonePrefixes = getStringArraySafely(res, R.array.phone_prefixes),
                wineAppellations = getStringArraySafely(res, R.array.wine_appellations),
                wineProducers = getStringArraySafely(res, R.array.wine_producers),
                wineKeywords = getStringArraySafely(res, R.array.wine_keywords),
                wineTitleKeywords = getStringArraySafely(res, R.array.wine_title_keywords),
                ocr3ExplicitWineHeaders = getStringArraySafely(res, R.array.ocr3_explicit_wine_headers),
                wineRemovePattern = getStringSafely(res, R.string.wine_remove_pattern),
                ocrSpellingRepairs = getStringArraySafely(res, R.array.ocr_spelling_repairs),
                adExclusions = getStringArraySafely(res, R.array.advertisement_exclusions),
                recipeCategoryHeaders = getStringArraySafely(res, R.array.recipe_category_headers),
                ocrNoiseExclusions = getStringArraySafely(res, R.array.ocr_noise_exclusions),
                titleKeywords = getStringArraySafely(res, R.array.title_keywords),
                mandatoryComplementIngredients = getStringArraySafely(res, R.array.ingredient_mandatory_complements),
                hangingConnectors = getStringArraySafely(res, R.array.hanging_connectors),
                ingredientVarietyQualifiers = getStringArraySafely(res, R.array.ingredient_variety_qualifiers),
                baseIngredientsExpectingVariety = getStringArraySafely(res, R.array.base_ingredients_expecting_variety),
                standaloneArticles = getStringArraySafely(res, R.array.standalone_articles),
                culinaryMeasurementUnits = getStringArraySafely(res, R.array.culinary_measurement_units),
                titleSpellingRepairs = getStringArraySafely(res, R.array.title_spelling_repairs),
                titleMultilineConnectors = getStringArraySafely(res, R.array.title_multiline_connectors),
                ingredientSplitterConnectors = getStringArraySafely(res, R.array.ingredient_splitter_connectors),
                ocrDiacriticRepairs = getStringArraySafely(res, R.array.ocr_diacritic_repairs),
                priceTitleKeywords = getStringArraySafely(res, R.array.price_title_keywords),
                priceCategoriesLow = getStringArraySafely(res, R.array.price_categories_low),
                priceCategoriesMedium = getStringArraySafely(res, R.array.price_categories_medium),
                priceCategoriesHigh = getStringArraySafely(res, R.array.price_categories_high),
                ingredientConnectors = getStringArraySafely(res, R.array.ingredient_connectors),
                ocrSentenceConjunctions = getStringArraySafely(res, R.array.ocr_sentence_conjunctions),
                sentenceEndingPunctuation = getStringArraySafely(res, R.array.sentence_ending_punctuation),
                protectedSectionHeaders = getStringArraySafely(res, R.array.ocr_protected_section_headers),
                timeHeaderPrefixes = getStringArraySafely(res, R.array.ocr_time_header_prefixes),
                timeHeaderKeywords = getStringArraySafely(res, R.array.ocr_time_header_keywords),
                ocr1ProtectedIngredientsHeaders = getStringArraySafely(res, R.array.ocr1_protected_ingredients_headers),
                ocr1ProtectedInstructionsHeaders = getStringArraySafely(res, R.array.ocr1_protected_instructions_headers),
                ocr2TechnicalDimensionKeywords = getStringArraySafely(res, R.array.ocr2_technical_dimension_keywords),
                genericWineWords = getStringArraySafely(res, R.array.generic_wine_words),
                orphanNounNumberWords = getStringArraySafely(res, R.array.orphan_noun_number_words),
                orphanNounStopWords = getStringArraySafely(res, R.array.orphan_noun_stop_words),
                narrativeStopWords = getStringArraySafely(res, R.array.narrative_stop_words),
                durationUnits = getStringArraySafely(res, R.array.duration_units),
                durationApproximationAdverbs = getStringArraySafely(res, R.array.duration_approximation_adverbs),
                stepStartVerbs = getStringArraySafely(res, R.array.step_start_verbs),
                stepSecondaryVerbs = getStringArraySafely(res, R.array.step_secondary_verbs),
                stepContextWords = getStringArraySafely(res, R.array.step_context_words),
                ocr1CanonicalIngredientsHeader = getStringSafely(res, R.string.ocr1_canonical_ingredients_header),
                ocr1CanonicalInstructionsHeader = getStringSafely(res, R.string.ocr1_canonical_instructions_header),
                ocr1IngredientsSuffixPrefix = getStringSafely(res, R.string.ocr1_ingredients_suffix_prefix),
                logOcr1ProtectedHeaderRepaired = getStringSafely(res, R.string.log_ocr1_protected_header_repaired),
                logOcr1ProtectedIngredientsEmpty = getStringSafely(res, R.string.log_ocr1_protected_ingredients_empty),
                logOcr1ProtectedInstructionsEmpty = getStringSafely(res, R.string.log_ocr1_protected_instructions_empty),
                logOcr2CleanerSourceCaptured = getStringSafely(res, R.string.log_ocr2_cleaner_source_captured),
                logOcr2CleanerNoiseRemoved = getStringSafely(res, R.string.log_ocr2_cleaner_noise_removed),
                logOcr2CleanerAdLineRemoved = getStringSafely(res, R.string.log_ocr2_cleaner_ad_line_removed),
                logOcr2CleanerAdPrefixRemoved = getStringSafely(res, R.string.log_ocr2_cleaner_ad_prefix_removed),
                logOcr2CleanerExcludedKeywordRemoved = getStringSafely(res, R.string.log_ocr2_cleaner_excluded_keyword_removed),
                logOcr3CategorizerNarrativeDelegation = getStringSafely(res, R.string.log_ocr3_categorizer_narrative_delegation),
                logOcr3CategorizerStart = getStringSafely(res, R.string.log_ocr3_categorizer_start),
                logOcr3CategorizerEnd = getStringSafely(res, R.string.log_ocr3_categorizer_end),
                logOcr3BlockAMetadataIgnored = getStringSafely(res, R.string.log_ocr3_block_a_metadata_ignored),
                logOcr3BlockAFusionContinuation = getStringSafely(res, R.string.log_ocr3_block_a_fusion_continuation),
                logOcr3BlockCReappearanceCheck = getStringSafely(res, R.string.log_ocr3_block_c_reappearance_check),
                logOcr3BlockCReappearanceMatch = getStringSafely(res, R.string.log_ocr3_block_c_reappearance_match),
                logOcr3BlockDClassificationSummary = getStringSafely(res, R.string.log_ocr3_block_d_classification_summary),
                logOcr3BlockEDispatchIngrSplit = getStringSafely(res, R.string.log_ocr3_block_e_dispatch_ingr_split),
                logOcr3BlockEDispatchInstrAdded = getStringSafely(res, R.string.log_ocr3_block_e_dispatch_instr_added),
                logOcr3BlockEDispatchCatchup = getStringSafely(res, R.string.log_ocr3_block_e_dispatch_catchup),
                logOcr3BlockEDispatchLockActivated = getStringSafely(res, R.string.log_ocr3_block_e_dispatch_lock_activated),
                logOcr3HeaderSwitchInstructions = getStringSafely(res, R.string.log_ocr3_header_switch_instructions),
                logOcr3HeaderSwitchIngredients = getStringSafely(res, R.string.log_ocr3_header_switch_ingredients),
                logOcr3HeaderSubheaderDetected = getStringSafely(res, R.string.log_ocr3_header_subheader_detected),
                logOcr3UnfinishedIngrStillEmpty = getStringSafely(res, R.string.log_ocr3_unfinished_ingr_still_empty),
                logOcr3UnfinishedIngrFused = getStringSafely(res, R.string.log_ocr3_unfinished_ingr_fused),
                logOcr3InvertedIsInverted = getStringSafely(res, R.string.log_ocr3_inverted_is_inverted),
                logOcr3InvertedStart = getStringSafely(res, R.string.log_ocr3_inverted_start),
                logOcr3InvertedEnd = getStringSafely(res, R.string.log_ocr3_inverted_end),
                logOcr3InvertedBottomIngrSwitch = getStringSafely(res, R.string.log_ocr3_inverted_bottom_ingr_switch),
                logOcr3NarrativeIsNarrative = getStringSafely(res, R.string.log_ocr3_narrative_is_narrative),
                logOcr3NarrativeStart = getStringSafely(res, R.string.log_ocr3_narrative_start),
                logOcr3NarrativeEnd = getStringSafely(res, R.string.log_ocr3_narrative_end),
                logOcr3NarrativeExtractedImpliedIngr = getStringSafely(res, R.string.log_ocr3_narrative_extracted_implied_ingr),
                logOcr3NarrativeExtractedSeasoning = getStringSafely(res, R.string.log_ocr3_narrative_extracted_seasoning),
                logOcr3NarrativeSeasoningAlreadyPresent = getStringSafely(res, R.string.log_ocr3_narrative_seasoning_already_present),
                logOcr3TitleExtractorStart = getStringSafely(res, R.string.log_ocr3_title_extractor_start),
                logOcr3TitleExtractorEnd = getStringSafely(res, R.string.log_ocr3_title_extractor_end),
                logOcr3TitleExtractorRetained = getStringSafely(res, R.string.log_ocr3_title_extractor_retained),
                logOcr3TitleExtractorIngrDetected = getStringSafely(res, R.string.log_ocr3_title_extractor_ingr_detected),
                logOcr3TitleExtractorNoIngr = getStringSafely(res, R.string.log_ocr3_title_extractor_no_ingr),
                logOcr3TitleExtractorLineProcessed = getStringSafely(res, R.string.log_ocr3_title_extractor_line_processed),
                logOcr3TitleExtractorCandidateScore = getStringSafely(res, R.string.log_ocr3_title_extractor_candidate_score),
                logOcr3TitleExtractorNoTitleRetained = getStringSafely(res, R.string.log_ocr3_title_extractor_no_title_retained),
                logOcr3TitleExtractorRemainingLine = getStringSafely(res, R.string.log_ocr3_title_extractor_remaining_line),
                logOcr3TitleExtractorSelPoivreCandidate = getStringSafely(res, R.string.log_ocr3_title_extractor_sel_poivre_candidate),
                logOcr3TitleRepairRuleInvalid = getStringSafely(res, R.string.log_ocr3_title_repair_rule_invalid),
                logOcr3TitleLettrineRepaired = getStringSafely(res, R.string.log_ocr3_title_lettrine_repaired),
                logOcr3TitleOrphanNounCompleted = getStringSafely(res, R.string.log_ocr3_title_orphan_noun_completed),
                logOcr3TitleNoOrphanNounFound = getStringSafely(res, R.string.log_ocr3_title_no_orphan_noun_found),
                logOcr3TitleStripRecipeNumberEmpty = getStringSafely(res, R.string.log_ocr3_title_strip_recipe_number_empty),
                logOcr3WineDuplicateIgnored = getStringSafely(res, R.string.log_ocr3_wine_duplicate_ignored),
                logOcr3WineEmptyIgnored = getStringSafely(res, R.string.log_ocr3_wine_empty_ignored),
                logOcr3WineSectionClosed = getStringSafely(res, R.string.log_ocr3_wine_section_closed),
                logOcr3WineSectionActive = getStringSafely(res, R.string.log_ocr3_wine_section_active),
                logOcr3WineAlreadyClosedIgnored = getStringSafely(res, R.string.log_ocr3_wine_already_closed_ignored),
                logOcr3WineImplicitVolumeClosed = getStringSafely(res, R.string.log_ocr3_wine_implicit_volume_closed),
                logOcr3WineImplicitNoVolumeOpen = getStringSafely(res, R.string.log_ocr3_wine_implicit_no_volume_open),
                logOcr3WineImplicitDuplicateIgnored = getStringSafely(res, R.string.log_ocr3_wine_implicit_duplicate_ignored),
                logOcr3WineImplicitEmptyIgnored = getStringSafely(res, R.string.log_ocr3_wine_implicit_empty_ignored),
                logOcr4SplitterDebugMatch = getStringSafely(res, R.string.log_ocr4_splitter_debug_match),
                logOcr5BeautifierInstructionMerged = getStringSafely(res, R.string.log_ocr5_beautifier_instruction_merged),
                logOcr5BeautifierVarietyMerged = getStringSafely(res, R.string.log_ocr5_beautifier_variety_merged),
                logOcr5BeautifierWineFormatted = getStringSafely(res, R.string.log_ocr5_beautifier_wine_formatted),
                logOcr5BeautifierSourceFormatted = getStringSafely(res, R.string.log_ocr5_beautifier_source_formatted),
                logOcr0RawCopyPasteStart = getStringSafely(res, R.string.log_ocr0_raw_copy_paste_start),
                logOcr0RawCopyPasteEnd = getStringSafely(res, R.string.log_ocr0_raw_copy_paste_end),
                logOcrVocabLoaderCustomDirNoCache = getStringSafely(res, R.string.log_ocr_vocab_loader_custom_dir_no_cache),
                logErrDiacriticRepairs = getStringSafely(res, R.string.log_err_diacritic_repairs)
            )
            OcrVocabularyLoader.cacheVocabularyIfNeeded(File("."), vocab)
            return vocab
        }

        /**
         * Charge de manière sécurisée un tableau de chaînes depuis les ressources sans lever d'exception.
         *
         * @param res Les ressources Android.
         * @param id L'identifiant de la ressource [R.array].
         * @return La liste des chaînes, ou une liste vide en cas d'erreur.
         */
        private fun getStringArraySafely(res: Resources, id: Int): List<String> {
            return try {
                res.getStringArray(id).toList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        /**
         * Charge de manière sécurisée une chaîne depuis les ressources sans lever d'exception.
         *
         * @param res Les ressources Android.
         * @param id L'identifiant de la ressource [R.string].
         * @return La chaîne, ou une chaîne vide en cas d'erreur.
         */
        private fun getStringSafely(res: Resources, id: Int): String {
            return try {
                (res.getString(id) as String?) ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }
}
