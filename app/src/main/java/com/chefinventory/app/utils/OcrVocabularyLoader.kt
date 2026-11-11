package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Chargeur data-driven responsable de l'ingestion, du parsing XML et de la mise en cache
 * du vocabulaire métier de l'application [OcrVocabulary].
 *
 * Responsabilités :
 * - Localiser et lire tous les répertoires res-values des 4 domaines fonctionnels depuis le disque (mode TDD/JVM) ou les ressources Android.
 * - Parser les fichiers XML (ocr_*.xml, step_keywords.xml, ingredients.xml, wine_arrays.xml) sans dépendance Android Framework.
 * - Effectuer la mise en cache de l'instance [OcrVocabulary] par défaut pour accélérer l'exécution des tests.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrVocabularyLoader {
    private const val TAG = "OcrVocabularyLoader"

    @Volatile
    private var cachedDefaultVocabulary: OcrVocabulary? = null

    /**
     * Charge le vocabulaire métier complet depuis les fichiers XML des répertoires res-values.
     *
     * @param projectDir Répertoire racine du projet (par défaut : dossier courant ".").
     * @return L'objet [OcrVocabulary] alimenté.
     * @throws IllegalStateException si aucun répertoire res-values n'est localisé.
     */
    fun loadFromProjectResources(projectDir: File = File(".")): OcrVocabulary {
        if ((projectDir.path == ".") && (cachedDefaultVocabulary != null) && cachedDefaultVocabulary!!.titleKeywords.size > 20) {
            return cachedDefaultVocabulary!!
        }

        val resDirs = findResValueDirs(projectDir)
        if (resDirs.isEmpty()) {
            val errorMsg = "Could not locate any 'res*/values' directory from: ${projectDir.absolutePath}"
            LogManager.w(TAG, errorMsg)
            if (projectDir.path != ".") {
                throw IllegalStateException(errorMsg)
            }
            return cachedDefaultVocabulary ?: OcrVocabulary()
        }

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        for (resDir in resDirs) {
            parseResDirectory(resDir, stringArrays, strings)
        }

        val combinedStepActionKeywords = getList(stringArrays, "step_start_verbs") +
                getList(stringArrays, "step_secondary_verbs") +
                getList(stringArrays, "step_context_words") +
                getList(stringArrays, "step_action_keywords") +
                getList(stringArrays, "step_action_keywords_complement")

        val vocabulary = OcrVocabulary(
            stepActionKeywords = combinedStepActionKeywords,
            commonIngredientsNoQty = getList(stringArrays, "common_ingredients_no_qty"),
            excludedOcrKeywords = getList(stringArrays, "excluded_ocr_keywords"),
            ingredientPreparationModifiers = getList(stringArrays, "ingredient_preparation_modifiers"),
            ingredientPreparationSuffixModifiers = getList(stringArrays, "ingredient_preparation_suffix_modifiers"),
            instructionSwitchKeywords = getList(stringArrays, "instruction_switch_keywords"),
            ingredientSemanticExclusions = getList(stringArrays, "ingredient_semantic_exclusions"),
            ingredientEndMarkers = getList(stringArrays, "ingredient_end_markers"),
            ingredientSubHeaders = getList(stringArrays, "ingredient_sub_headers"),
            sourceKeywords = getList(stringArrays, "source_keywords"),
            knownSourceBrands = getList(stringArrays, "known_source_brands"),
            phonePrefixes = getList(stringArrays, "phone_prefixes"),
            wineAppellations = getList(stringArrays, "wine_appellations"),
            wineProducers = getList(stringArrays, "wine_producers"),
            wineKeywords = getList(stringArrays, "wine_keywords"),
            wineTitleKeywords = getList(stringArrays, "wine_title_keywords"),
            ocr3ExplicitWineHeaders = getList(stringArrays, "ocr3_explicit_wine_headers"),
            wineRemovePattern = getStringVal(strings, "wine_remove_pattern"),
            ocrSpellingRepairs = getList(stringArrays, "ocr_spelling_repairs"),
            adExclusions = getList(stringArrays, "advertisement_exclusions"),
            recipeCategoryHeaders = getList(stringArrays, "recipe_category_headers"),
            ocrNoiseExclusions = getList(stringArrays, "ocr_noise_exclusions"),
            titleKeywords = getList(stringArrays, "title_keywords"),
            mandatoryComplementIngredients = getList(stringArrays, "ingredient_mandatory_complements"),
            hangingConnectors = getList(stringArrays, "hanging_connectors"),
            ingredientVarietyQualifiers = getList(stringArrays, "ingredient_variety_qualifiers"),
            baseIngredientsExpectingVariety = getList(stringArrays, "base_ingredients_expecting_variety"),
            standaloneArticles = getList(stringArrays, "standalone_articles"),
            culinaryMeasurementUnits = getList(stringArrays, "culinary_measurement_units"),
            titleSpellingRepairs = getList(stringArrays, "title_spelling_repairs"),
            titleMultilineConnectors = getList(stringArrays, "title_multiline_connectors"),
            ingredientSplitterConnectors = getList(stringArrays, "ingredient_splitter_connectors"),
            ocrDiacriticRepairs = getList(stringArrays, "ocr_diacritic_repairs"),
            priceTitleKeywords = getList(stringArrays, "price_title_keywords"),
            priceCategoriesLow = getList(stringArrays, "price_categories_low"),
            priceCategoriesMedium = getList(stringArrays, "price_categories_medium"),
            priceCategoriesHigh = getList(stringArrays, "price_categories_high"),
            ingredientConnectors = getList(stringArrays, "ingredient_connectors"),
            ocrSentenceConjunctions = getList(stringArrays, "ocr_sentence_conjunctions"),
            sentenceEndingPunctuation = getList(stringArrays, "sentence_ending_punctuation"),
            protectedSectionHeaders = getList(stringArrays, "ocr_protected_section_headers"),
            timeHeaderPrefixes = getList(stringArrays, "ocr_time_header_prefixes"),
            timeHeaderKeywords = getList(stringArrays, "ocr_time_header_keywords"),
            ocr1ProtectedIngredientsHeaders = getList(stringArrays, "ocr1_protected_ingredients_headers"),
            ocr1ProtectedInstructionsHeaders = getList(stringArrays, "ocr1_protected_instructions_headers"),
            ocr2TechnicalDimensionKeywords = getList(stringArrays, "ocr2_technical_dimension_keywords"),
            genericWineWords = getList(stringArrays, "generic_wine_words"),
            orphanNounNumberWords = getList(stringArrays, "orphan_noun_number_words"),
            orphanNounStopWords = getList(stringArrays, "orphan_noun_stop_words"),
            narrativeStopWords = getList(stringArrays, "narrative_stop_words"),
            durationUnits = getList(stringArrays, "duration_units"),
            durationApproximationAdverbs = getList(stringArrays, "duration_approximation_adverbs"),
            stepStartVerbs = getList(stringArrays, "step_start_verbs"),
            stepSecondaryVerbs = getList(stringArrays, "step_secondary_verbs"),
            stepContextWords = getList(stringArrays, "step_context_words"),
            ocr1CanonicalIngredientsHeader = getStringVal(strings, "ocr1_canonical_ingredients_header"),
            ocr1CanonicalInstructionsHeader = getStringVal(strings, "ocr1_canonical_instructions_header"),
            ocr1IngredientsSuffixPrefix = getStringVal(strings, "ocr1_ingredients_suffix_prefix"),
            logOcr1ProtectedHeaderRepaired = getStringVal(strings, "log_ocr1_protected_header_repaired"),
            logOcr1ProtectedIngredientsEmpty = getStringVal(strings, "log_ocr1_protected_ingredients_empty"),
            logOcr1ProtectedInstructionsEmpty = getStringVal(strings, "log_ocr1_protected_instructions_empty"),
            logOcr2CleanerSourceCaptured = getStringVal(strings, "log_ocr2_cleaner_source_captured"),
            logOcr2CleanerNoiseRemoved = getStringVal(strings, "log_ocr2_cleaner_noise_removed"),
            logOcr2CleanerAdLineRemoved = getStringVal(strings, "log_ocr2_cleaner_ad_line_removed"),
            logOcr2CleanerAdPrefixRemoved = getStringVal(strings, "log_ocr2_cleaner_ad_prefix_removed"),
            logOcr2CleanerExcludedKeywordRemoved = getStringVal(strings, "log_ocr2_cleaner_excluded_keyword_removed"),
            logOcr3CategorizerNarrativeDelegation = getStringVal(strings, "log_ocr3_categorizer_narrative_delegation"),
            logOcr3CategorizerStart = getStringVal(strings, "log_ocr3_categorizer_start"),
            logOcr3CategorizerEnd = getStringVal(strings, "log_ocr3_categorizer_end"),
            logOcr3BlockAMetadataIgnored = getStringVal(strings, "log_ocr3_block_a_metadata_ignored"),
            logOcr3BlockAFusionContinuation = getStringVal(strings, "log_ocr3_block_a_fusion_continuation"),
            logOcr3BlockCReappearanceCheck = getStringVal(strings, "log_ocr3_block_c_reappearance_check"),
            logOcr3BlockCReappearanceMatch = getStringVal(strings, "log_ocr3_block_c_reappearance_match"),
            logOcr3BlockDClassificationSummary = getStringVal(strings, "log_ocr3_block_d_classification_summary"),
            logOcr3BlockEDispatchIngrSplit = getStringVal(strings, "log_ocr3_block_e_dispatch_ingr_split"),
            logOcr3BlockEDispatchInstrAdded = getStringVal(strings, "log_ocr3_block_e_dispatch_instr_added"),
            logOcr3BlockEDispatchCatchup = getStringVal(strings, "log_ocr3_block_e_dispatch_catchup"),
            logOcr3BlockEDispatchLockActivated = getStringVal(strings, "log_ocr3_block_e_dispatch_lock_activated"),
            logOcr3HeaderSwitchInstructions = getStringVal(strings, "log_ocr3_header_switch_instructions"),
            logOcr3HeaderSwitchIngredients = getStringVal(strings, "log_ocr3_header_switch_ingredients"),
            logOcr3HeaderSubheaderDetected = getStringVal(strings, "log_ocr3_header_subheader_detected"),
            logOcr3UnfinishedIngrStillEmpty = getStringVal(strings, "log_ocr3_unfinished_ingr_still_empty"),
            logOcr3UnfinishedIngrFused = getStringVal(strings, "log_ocr3_unfinished_ingr_fused"),
            logOcr3InvertedIsInverted = getStringVal(strings, "log_ocr3_inverted_is_inverted"),
            logOcr3InvertedStart = getStringVal(strings, "log_ocr3_inverted_start"),
            logOcr3InvertedEnd = getStringVal(strings, "log_ocr3_inverted_end"),
            logOcr3InvertedBottomIngrSwitch = getStringVal(strings, "log_ocr3_inverted_bottom_ingr_switch"),
            logOcr3NarrativeIsNarrative = getStringVal(strings, "log_ocr3_narrative_is_narrative"),
            logOcr3NarrativeStart = getStringVal(strings, "log_ocr3_narrative_start"),
            logOcr3NarrativeEnd = getStringVal(strings, "log_ocr3_narrative_end"),
            logOcr3NarrativeExtractedImpliedIngr = getStringVal(strings, "log_ocr3_narrative_extracted_implied_ingr"),
            logOcr3NarrativeExtractedSeasoning = getStringVal(strings, "log_ocr3_narrative_extracted_seasoning"),
            logOcr3NarrativeSeasoningAlreadyPresent = getStringVal(strings, "log_ocr3_narrative_seasoning_already_present"),
            logOcr3TitleExtractorStart = getStringVal(strings, "log_ocr3_title_extractor_start"),
            logOcr3TitleExtractorEnd = getStringVal(strings, "log_ocr3_title_extractor_end"),
            logOcr3TitleExtractorRetained = getStringVal(strings, "log_ocr3_title_extractor_retained"),
            logOcr3TitleExtractorIngrDetected = getStringVal(strings, "log_ocr3_title_extractor_ingr_detected"),
            logOcr3TitleExtractorNoIngr = getStringVal(strings, "log_ocr3_title_extractor_no_ingr"),
            logOcr3TitleExtractorLineProcessed = getStringVal(strings, "log_ocr3_title_extractor_line_processed"),
            logOcr3TitleExtractorCandidateScore = getStringVal(strings, "log_ocr3_title_extractor_candidate_score"),
            logOcr3TitleExtractorNoTitleRetained = getStringVal(strings, "log_ocr3_title_extractor_no_title_retained"),
            logOcr3TitleExtractorRemainingLine = getStringVal(strings, "log_ocr3_title_extractor_remaining_line"),
            logOcr3TitleExtractorSelPoivreCandidate = getStringVal(strings, "log_ocr3_title_extractor_sel_poivre_candidate"),
            logOcr3TitleRepairRuleInvalid = getStringVal(strings, "log_ocr3_title_repair_rule_invalid"),
            logOcr3TitleLettrineRepaired = getStringVal(strings, "log_ocr3_title_lettrine_repaired"),
            logOcr3TitleOrphanNounCompleted = getStringVal(strings, "log_ocr3_title_orphan_noun_completed"),
            logOcr3TitleNoOrphanNounFound = getStringVal(strings, "log_ocr3_title_no_orphan_noun_found"),
            logOcr3TitleStripRecipeNumberEmpty = getStringVal(strings, "log_ocr3_title_strip_recipe_number_empty"),
            logOcr3WineDuplicateIgnored = getStringVal(strings, "log_ocr3_wine_duplicate_ignored"),
            logOcr3WineEmptyIgnored = getStringVal(strings, "log_ocr3_wine_empty_ignored"),
            logOcr3WineSectionClosed = getStringVal(strings, "log_ocr3_wine_section_closed"),
            logOcr3WineSectionActive = getStringVal(strings, "log_ocr3_wine_section_active"),
            logOcr3WineAlreadyClosedIgnored = getStringVal(strings, "log_ocr3_wine_already_closed_ignored"),
            logOcr3WineImplicitVolumeClosed = getStringVal(strings, "log_ocr3_wine_implicit_volume_closed"),
            logOcr3WineImplicitNoVolumeOpen = getStringVal(strings, "log_ocr3_wine_implicit_no_volume_open"),
            logOcr3WineImplicitDuplicateIgnored = getStringVal(strings, "log_ocr3_wine_implicit_duplicate_ignored"),
            logOcr3WineImplicitEmptyIgnored = getStringVal(strings, "log_ocr3_wine_implicit_empty_ignored"),
            logOcr4SplitterDebugMatch = getStringVal(strings, "log_ocr4_splitter_debug_match"),
            logOcr5BeautifierInstructionMerged = getStringVal(strings, "log_ocr5_beautifier_instruction_merged"),
            logOcr5BeautifierVarietyMerged = getStringVal(strings, "log_ocr5_beautifier_variety_merged"),
            logOcr5BeautifierWineFormatted = getStringVal(strings, "log_ocr5_beautifier_wine_formatted"),
            logOcr5BeautifierSourceFormatted = getStringVal(strings, "log_ocr5_beautifier_source_formatted"),
            logOcr0RawCopyPasteStart = getStringVal(strings, "log_ocr0_raw_copy_paste_start"),
            logOcr0RawCopyPasteEnd = getStringVal(strings, "log_ocr0_raw_copy_paste_end"),
            logOcrVocabLoaderCustomDirNoCache = getStringVal(strings, "log_ocr_vocab_loader_custom_dir_no_cache"),
            logErrDiacriticRepairs = getStringVal(strings, "log_err_diacritic_repairs")
        )

        cacheVocabularyIfNeeded(projectDir, vocabulary)
        return vocabulary
    }

    /**
     * Effectue la mise en cache de l'instance [OcrVocabulary] par défaut si le répertoire est le répertoire racine courant.
     *
     * @param projectDir Le répertoire racine du projet.
     * @param vocabulary L'instance [OcrVocabulary] à mettre en cache.
     */
    internal fun cacheVocabularyIfNeeded(projectDir: File, vocabulary: OcrVocabulary) {
        if (projectDir.path == "." && vocabulary.titleKeywords.size > 20) {
            cachedDefaultVocabulary = vocabulary
        } else {
            val format = vocabulary.logOcrVocabLoaderCustomDirNoCache
            LogManager.d(TAG, String.format(format, projectDir.path))
        }
    }

    /**
     * Réinitialise le cache interne du vocabulaire (utilisé pour l'isolation des tests unitaires).
     */
    internal fun clearCache() {
        cachedDefaultVocabulary = null
    }

    /**
     * Localise l'ensemble des répertoires `values/` existants à travers les 4 domaines de ressources (`res`, `res-pantry`, `res-catalog`, `res-ocr`).
     *
     * @param startDir Le répertoire initial de recherche.
     * @return La liste des dossiers `values/` localisés.
     */
    private fun findResValueDirs(startDir: File): List<File> {
        val baseCandidates = listOf(
            File(startDir, "src/main"),
            File(startDir, "app/src/main"),
            File(startDir, "../app/src/main"),
            File(startDir, "../../app/src/main"),
        )
        val subFolders = listOf("res", "res-pantry", "res-catalog", "res-ocr")
        val foundDirs = mutableListOf<File>()

        for (base in baseCandidates) {
            for (sub in subFolders) {
                val candidateValues = File(base, "$sub/values")
                if (candidateValues.isDirectory && !foundDirs.contains(candidateValues)) {
                    foundDirs.add(candidateValues)
                }
            }
        }
        return foundDirs
    }

    /**
     * Lit et analyse tous les fichiers XML de ressources contenus dans le répertoire [resDir].
     *
     * @param resDir Le répertoire de ressources `values/` à analyser.
     * @param stringArrays La carte de destination des tableaux de chaînes.
     * @param strings La carte de destination des chaînes simples.
     */
    internal fun parseResDirectory(
        resDir: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>
    ) {
        val xmlFiles = resDir.listFiles { _, name -> name.endsWith(".xml") }
        if (xmlFiles != null) {
            for (xmlFile in xmlFiles) {
                parseXmlResourceFile(xmlFile, stringArrays, strings)
            }
        } else {
            val format = getStringVal(strings, "log_ocr_vocab_loader_no_xml_files")
            LogManager.w(TAG, String.format(format, resDir.absolutePath))
        }
    }

    /**
     * Parse un fichier XML individuel de ressources à l'aide du parser DOM.
     *
     * @param file Le fichier XML à parser.
     * @param stringArrays La carte de destination des tableaux de chaînes.
     * @param strings La carte de destination des chaînes simples.
     */
    internal fun parseXmlResourceFile(
        file: File,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>,
    ) {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            parseXmlDocument(doc, stringArrays, strings)
        } catch (e: Exception) {
            val format = getStringVal(strings, "log_err_ocr_vocab_loader_parse_file")
            LogManager.e(TAG, String.format(format, file.name, e.message), e)
        }
    }

    /**
     * Extrait les éléments `<string-array>` et `<string>` d'un document DOM XML normalisé.
     *
     * @param doc Le document XML DOM normalisé.
     * @param stringArrays La carte de destination des tableaux de chaînes.
     * @param strings La carte de destination des chaînes simples.
     */
    internal fun parseXmlDocument(
        doc: Document,
        stringArrays: MutableMap<String, List<String>>,
        strings: MutableMap<String, String>,
    ) {
        doc.documentElement.normalize()

        val arrayNodes = doc.getElementsByTagName("string-array")
        for (i in 0 until arrayNodes.length) {
            val node = arrayNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val elem = node as Element
                val arrayName = if (elem.hasAttribute("arrayName")) elem.getAttribute("arrayName") else elem.getAttribute("name")
                val items = mutableListOf<String>()
                val itemNodes = elem.getElementsByTagName("item")
                for (j in 0 until itemNodes.length) {
                    // Purge des retours chariot Windows (\r) lors du parsing XML DOM :
                    val itemText = itemNodes.item(j).textContent.replace("\r", "").trim()
                    .replace("\\'", "'")
                    .replace("\\?", "?")
                    val parts = itemText.split("|")
                    if (parts.size == 2 && parts[0].trim().equals(parts[1].trim(), ignoreCase = true)) {
                        continue
                    }
                    items.add(itemText)
                }
                if (arrayName.isNotBlank()) {
                    val existing = stringArrays[arrayName] ?: emptyList()
                    stringArrays[arrayName] = existing + items
                }
            } else {
                val msg = getStringVal(strings, "log_ocr_vocab_loader_skip_non_element_array")
                LogManager.w(TAG, msg)
            }
        }

        val stringNodes = doc.getElementsByTagName("string")
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val elem = node as Element
                val stringName = elem.getAttribute("name")
                val rawText = elem.textContent.replace("\r", "").trim()
                if (stringName.isNotBlank()) {
                    strings[stringName] = rawText.replace("\\'", "'")
                }
            } else {
                val msg = getStringVal(strings, "log_ocr_vocab_loader_skip_non_element_string")
                LogManager.w(TAG, msg)
            }
        }
    }

    /**
     * Extrait de manière sécurisée une liste de chaînes depuis la carte [map] pour une clé [key] donnée.
     *
     * @param map La carte source des tableaux de chaînes.
     * @param key La clé de tableau recherchée.
     * @return La liste des chaînes correspondantes, ou une liste vide.
     */
    internal fun getList(map: Map<String, List<String>>, key: String): List<String> {
        return map[key] ?: emptyList()
    }

    /**
     * Extrait de manière sécurisée une chaîne simple depuis la carte [map] pour une clé [key] donnée.
     *
     * @param map La carte source des chaînes simples.
     * @param key La clé de chaîne recherchée.
     * @return La valeur de la chaîne correspondante, ou une chaîne vide.
     */
    internal fun getStringVal(map: Map<String, String>, key: String): String {
        return map[key] ?: ""
    }
}
