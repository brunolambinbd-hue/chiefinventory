package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

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
 * @property logOcr3TitleNoOrphanNounFound Message de log lorsqu'aucun nom orphelin n'est trouvé.
 * @property logOcr3TitleStripRecipeNumberEmpty Message de log pour une chaîne vide après suppression du numéro de fiche.
 * @property logOcr3WineDuplicateIgnored Message de log pour la ligne de vin doublon ignorée d'étape 3.
 * @property logOcr3WineEmptyIgnored Message de log pour la ligne de vin vide ignorée d'étape 3.
 * @property logOcr3WineSectionClosed Message de log pour la fermeture de section vin d'étape 3.
 * @property logOcr3WineSectionActive Message de log pour le maintien de section vin d'étape 3.
 * @property logOcr3WineAlreadyClosedIgnored Message de log pour la ligne de vin ignorée sur section fermée d'étape 3.
 * @property logOcr3WineImplicitVolumeClosed Message de log pour la fermeture de section vin implicite d'étape 3.
 * @property logOcr3WineImplicitNoVolumeOpen Message de log pour le vin implicite sans volume d'étape 3.
 * @property logOcr3WineImplicitDuplicateIgnored Message de log pour le vin implicite doublon ignoré d'étape 3.
 * @property logOcr3WineImplicitEmptyIgnored Message de log pour le vin implicite vide ignoré d'étape 3.
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
    val ocr2TechnicalDimensionKeywords: List<String> = emptyList(),
    val orphanNounNumberWords: List<String> = emptyList(),
    val orphanNounStopWords: List<String> = emptyList(),
    val ocr1CanonicalIngredientsHeader: String = "ingrédients",
    val ocr1CanonicalInstructionsHeader: String = "instructions",
    val ocr1IngredientsSuffixPrefix: String = "ng",
    val logOcr1ProtectedHeaderRepaired: String = "Réparation en-tête OCR : '%1\$s' ➔ '%2\$s'",
    val logOcr1ProtectedIngredientsEmpty: String = "Attention : ocr1ProtectedIngredientsHeaders est vide dans le vocabulaire",
    val logOcr1ProtectedInstructionsEmpty: String = "Attention : ocr1ProtectedInstructionsHeaders est vide dans le vocabulaire",
    val logOcr2CleanerSourceCaptured: String = "CLEANER : Source précoce capturée depuis bannière ➔ '%1\$s'",
    val logOcr2CleanerNoiseRemoved: String = "CLEANER : Bruit de scan OCR supprimé ➔ '%1\$s'",
    val logOcr2CleanerAdLineRemoved: String = "CLEANER : Ligne publicitaire supprimée ➔ '%1\$s'",
    val logOcr2CleanerAdPrefixRemoved: String = "CLEANER : Préfixe publicitaire effacé de ➔ '%1\$s'",
    val logOcr2CleanerExcludedKeywordRemoved: String = "CLEANER : Mot-clé exclu supprimé ➔ '%1\$s'",
    val logOcr3CategorizerNarrativeDelegation: String = " => ACTIVER DÉLÉGATION NARRATIVE TYPE 2 (Ocr3NarrativeCategorizer)",
    val logOcr3CategorizerStart: String = "--- DÉBUT CATÉGORISATION ---",
    val logOcr3CategorizerEnd: String = "--- FIN CATÉGORISATION ---",
    val logOcr3BlockAMetadataIgnored: String = "  [MÉTADONNÉES / PÉRIODIQUE IGNORÉ] '%1\$s'",
    val logOcr3BlockAFusionContinuation: String = "  [FUSION CONTINUATION] Fusionné : '%1\$s' ➔ Nouvelle ligne : '%2\$s'",
    val logOcr3BlockCReappearanceCheck: String = "LIGNE: '%1\$s' | Déjà inventorisés: %2\$s",
    val logOcr3BlockCReappearanceMatch: String = "  ? Comparaison: '%1\$s' -> MATCH=%2\$b | hardQty=%3\$b | weight=%4\$b | closed=%5\$b | final=%6\$b",
    val logOcr3BlockDClassificationSummary: String = "  => BILAN: reappear=%1\$b | instrSignal=%2\$b | currentSec=%3\$d",
    val logOcr3BlockEDispatchIngrSplit: String = "  [INGR] Split: Q=%1\$s, U=%2\$s, N='%3\$s', Info='%4\$s' | Keywords added: %5\$s",
    val logOcr3BlockEDispatchInstrAdded: String = "  [INSTR] Ligne ajoutée aux instructions ➔ '%1\$s'",
    val logOcr3BlockEDispatchCatchup: String = "  [RATTRAPAGE INGRÉDIENT] En-tête rattrapé depuis section fermée ➔ '%1\$s'",
    val logOcr3BlockEDispatchLockActivated: String = "  [!] VERROU ACTIVÉ (Marqueur de fin détecté)",
    val logOcr3HeaderSwitchInstructions: String = "  [!] BASCULEMENT DE SECTION ➔ INSTRUCTIONS ('%1\$s')",
    val logOcr3HeaderSwitchIngredients: String = "  [!] BASCULEMENT DE SECTION ➔ INGRÉDIENTS ('%1\$s')",
    val logOcr3HeaderSubheaderDetected: String = "  [!] SOUS-EN-TÊTE D'INGRÉDIENT DÉTECTÉ ('%1\$s')",
    val logOcr3UnfinishedIngrStillEmpty: String = "  [FUSION UNITÉ INCOMPLÈTE] Nom d'ingrédient toujours vide après fusion ('%1\$s')",
    val logOcr3UnfinishedIngrFused: String = "  [FUSION UNITÉ INCOMPLÈTE] Fusionné : '%1\$s' ➔ Nouvelle ligne : '%2\$s'",
    val logOcr3TitleExtractorStart: String = "=== DEBUT EXTRACTION DU TITRE PAR SCORING ===",
    val logOcr3TitleExtractorEnd: String = "=== FIN EXTRACTION DU TITRE PAR SCORING ===",
    val logOcr3TitleExtractorRetained: String = ">>> TITRE RETENU INITIALEMENT: '%1\$s' avec SCORE MAX: %2\$d",
    val logOcr3TitleExtractorIngrDetected: String = "TITLE EXTRACTOR : Ingrédients détectés sur la fiche pour scoring ➔ %1\$s",
    val logOcr3TitleExtractorNoIngr: String = "TITLE EXTRACTOR : Aucun mot d'ingrédient détecté pour scoring",
    val logOcr3TitleExtractorLineProcessed: String = "  [LIGNE TRAITÉE] '%1\$s'",
    val logOcr3TitleExtractorCandidateScore: String = "TITLE CANDIDATE [#%1\$d]: '%2\$s' ➔ SCORE: %3\$d (%4\$s)",
    val logOcr3TitleExtractorNoTitleRetained: String = ">>> AUCUN TITRE VALIDE RETENU",
    val logOcr3TitleExtractorRemainingLine: String = "  [LIGNE RESTANTE CANDIDATE] '%1\$s'",
    val logOcr3TitleExtractorSelPoivreCandidate: String = "TITLE EXTRACTOR : Candidat extrait depuis la ligne sel/poivre ➔ '%1\$s' (score=%2\$d)",
    val logOcr3TitleRepairRuleInvalid: String = "  [SKIP] Règle de réparation invalide sans séparateur : '%1\$s'",
    val logOcr3TitleLettrineRepaired: String = "TITLE EXTRACTOR : Lettrine réparée ➔ '%1\$s' devenant '%2\$s' (via '%3\$s')",
    val logOcr3TitleOrphanNounCompleted: String = "TITLE EXTRACTOR : Titre complété par nom orphelin ➔ '%1\$s' (ligne consommée : '%2\$s')",
    val logOcr3TitleNoOrphanNounFound: String = "  [INFO] Aucun nom orphelin trouvé dans les lignes restantes",
    val logOcr3TitleStripRecipeNumberEmpty: String = "  [INFO] Chaîne vide après suppression du numéro de fiche",
    val logOcr3WineDuplicateIgnored: String = "  [!] Ligne de vin ignorée (doublon déjà enregistré)",
    val logOcr3WineEmptyIgnored: String = "  [!] Ligne de vin ignorée (nom vide)",
    val logOcr3WineSectionClosed: String = "  [!] SECTION VIN FERMÉE (Volume/Millésime/Enseigne vin détectée)",
    val logOcr3WineSectionActive: String = "  [!] Section vin maintenue active (en attente du volume/millésime final)",
    val logOcr3WineAlreadyClosedIgnored: String = "  [!] Ligne de vin ignorée (section vin déjà fermée)",
    val logOcr3WineImplicitVolumeClosed: String = "  [!] SECTION VIN FERMÉE (Volume/Millésime détecté)",
    val logOcr3WineImplicitNoVolumeOpen: String = "  [!] Vin implicite capturé sans volume (section vin reste ouverte)",
    val logOcr3WineImplicitDuplicateIgnored: String = "  [!] Ligne de vin implicite ignorée (doublon déjà enregistré)",
    val logOcr3WineImplicitEmptyIgnored: String = "  [!] Ligne de vin implicite ignorée (nom vide)",
    val logOcrVocabLoaderCustomDirNoCache: String = "Répertoire personnalisé '%1\$s', non mis en cache",
    val logErrDiacriticRepairs: String = "Échec du chargement des réparations diacritiques par défaut"
) {
    companion object {

        /**
         * Construit l'objet [OcrVocabulary] depuis les ressources Android d'une instance [Resources].
         *
         * @param res Les ressources Android de l'application.
         * @return L'objet [OcrVocabulary] alimenté.
         */
        fun fromResources(res: Resources): OcrVocabulary {
            return OcrVocabulary(
                stepActionKeywords = getStringArraySafely(res, R.array.step_action_keywords) + getStringArraySafely(res, R.array.step_action_keywords_complement),
                commonIngredientsNoQty = getStringArraySafely(res, R.array.common_ingredients_no_qty),
                excludedOcrKeywords = getStringArraySafely(res, R.array.excluded_ocr_keywords),
                ingredientPreparationModifiers = getStringArraySafely(res, R.array.ingredient_preparation_modifiers),
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
                protectedSectionHeaders = getStringArraySafely(res, R.array.ocr_protected_section_headers),
                timeHeaderPrefixes = getStringArraySafely(res, R.array.ocr_time_header_prefixes),
                timeHeaderKeywords = getStringArraySafely(res, R.array.ocr_time_header_keywords),
                ocr1ProtectedIngredientsHeaders = getStringArraySafely(res, R.array.ocr1_protected_ingredients_headers),
                ocr1ProtectedInstructionsHeaders = getStringArraySafely(res, R.array.ocr1_protected_instructions_headers),
                ocr2TechnicalDimensionKeywords = getStringArraySafely(res, R.array.ocr2_technical_dimension_keywords),
                orphanNounNumberWords = getStringArraySafely(res, R.array.orphan_noun_number_words),
                orphanNounStopWords = getStringArraySafely(res, R.array.orphan_noun_stop_words),
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
                logOcrVocabLoaderCustomDirNoCache = getStringSafely(res, R.string.log_ocr_vocab_loader_custom_dir_no_cache),
                logErrDiacriticRepairs = getStringSafely(res, R.string.log_err_diacritic_repairs)
            )
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
