package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class OcrVocabularyTest {

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testOcrVocabularySyntheticDefaultConstructorReflectionCoverage() {
        val defaultConstructor = OcrVocabulary::class.java.declaredConstructors.firstOrNull { c ->
            c.parameterTypes.any { it.name.contains("DefaultConstructorMarker") }
        }

        if (defaultConstructor != null) {
            defaultConstructor.isAccessible = true
            val paramCount = defaultConstructor.parameterCount

            val argsMask0 = Array<Any?>(paramCount) { i ->
                val pType = defaultConstructor.parameterTypes[i]
                when {
                    pType == String::class.java -> ""
                    pType == Int::class.javaPrimitiveType || pType == Int::class.java -> 0
                    pType == List::class.java -> emptyList<String>()
                    else -> null
                }
            }
            defaultConstructor.newInstance(*argsMask0)

            val argsMaskAllOn = Array<Any?>(paramCount) { i ->
                val pType = defaultConstructor.parameterTypes[i]
                when {
                    pType == String::class.java -> ""
                    pType == Int::class.javaPrimitiveType || pType == Int::class.java -> -1
                    pType == List::class.java -> null
                    else -> null
                }
            }
            defaultConstructor.newInstance(*argsMaskAllOn)
        }
    }

    @Test
    fun testOcrVocabularyIndividualPropertyCopiesCoverage() {
        val base = OcrVocabulary()

        base.copy(stepActionKeywords = listOf("x"))
        base.copy(commonIngredientsNoQty = listOf("x"))
        base.copy(excludedOcrKeywords = listOf("x"))
        base.copy(ingredientPreparationModifiers = listOf("x"))
        base.copy(instructionSwitchKeywords = listOf("x"))
        base.copy(ingredientSemanticExclusions = listOf("x"))
        base.copy(ingredientEndMarkers = listOf("x"))
        base.copy(ingredientSubHeaders = listOf("x"))
        base.copy(sourceKeywords = listOf("x"))
        base.copy(knownSourceBrands = listOf("x"))
        base.copy(phonePrefixes = listOf("x"))
        base.copy(wineAppellations = listOf("x"))
        base.copy(wineProducers = listOf("x"))
        base.copy(wineKeywords = listOf("x"))
        base.copy(wineTitleKeywords = listOf("x"))
        base.copy(ocr3ExplicitWineHeaders = listOf("x"))
        base.copy(wineRemovePattern = "x")
        base.copy(ocrSpellingRepairs = listOf("x"))
        base.copy(adExclusions = listOf("x"))
        base.copy(ocrNoiseExclusions = listOf("x"))
        base.copy(titleKeywords = listOf("x"))
        base.copy(mandatoryComplementIngredients = listOf("x"))
        base.copy(hangingConnectors = listOf("x"))
        base.copy(ingredientVarietyQualifiers = listOf("x"))
        base.copy(baseIngredientsExpectingVariety = listOf("x"))
        base.copy(standaloneArticles = listOf("x"))
        base.copy(culinaryMeasurementUnits = listOf("x"))
        base.copy(titleSpellingRepairs = listOf("x"))
        base.copy(titleMultilineConnectors = listOf("x"))
        base.copy(ingredientSplitterConnectors = listOf("x"))
        base.copy(ocrDiacriticRepairs = listOf("x"))
        base.copy(priceTitleKeywords = listOf("x"))
        base.copy(priceCategoriesLow = listOf("x"))
        base.copy(priceCategoriesMedium = listOf("x"))
        base.copy(priceCategoriesHigh = listOf("x"))
        base.copy(ingredientConnectors = listOf("x"))
        base.copy(protectedSectionHeaders = listOf("x"))
        base.copy(timeHeaderPrefixes = listOf("x"))
        base.copy(timeHeaderKeywords = listOf("x"))
        base.copy(ocr1ProtectedIngredientsHeaders = listOf("x"))
        base.copy(ocr1ProtectedInstructionsHeaders = listOf("x"))
        base.copy(ocr2TechnicalDimensionKeywords = listOf("x"))
        base.copy(orphanNounNumberWords = listOf("x"))
        base.copy(orphanNounStopWords = listOf("x"))
        base.copy(narrativeStopWords = listOf("x"))
        base.copy(durationUnits = listOf("x"))
        base.copy(stepStartVerbs = listOf("x"))
        base.copy(stepSecondaryVerbs = listOf("x"))
        base.copy(stepContextWords = listOf("x"))
        base.copy(ocr1CanonicalIngredientsHeader = "x")
        base.copy(ocr1CanonicalInstructionsHeader = "x")
        base.copy(ocr1IngredientsSuffixPrefix = "x")
        base.copy(logOcr1ProtectedHeaderRepaired = "x")
        base.copy(logOcr1ProtectedIngredientsEmpty = "x")
        base.copy(logOcr1ProtectedInstructionsEmpty = "x")
        base.copy(logOcr2CleanerSourceCaptured = "x")
        base.copy(logOcr2CleanerNoiseRemoved = "x")
        base.copy(logOcr2CleanerAdLineRemoved = "x")
        base.copy(logOcr2CleanerAdPrefixRemoved = "x")
        base.copy(logOcr2CleanerExcludedKeywordRemoved = "x")
        base.copy(logOcr3CategorizerNarrativeDelegation = "x")
        base.copy(logOcr3CategorizerStart = "x")
        base.copy(logOcr3CategorizerEnd = "x")
        base.copy(logOcr3BlockAMetadataIgnored = "x")
        base.copy(logOcr3BlockAFusionContinuation = "x")
        base.copy(logOcr3BlockCReappearanceCheck = "x")
        base.copy(logOcr3BlockCReappearanceMatch = "x")
        base.copy(logOcr3BlockDClassificationSummary = "x")
        base.copy(logOcr3BlockEDispatchIngrSplit = "x")
        base.copy(logOcr3BlockEDispatchInstrAdded = "x")
        base.copy(logOcr3BlockEDispatchCatchup = "x")
        base.copy(logOcr3BlockEDispatchLockActivated = "x")
        base.copy(logOcr3HeaderSwitchInstructions = "x")
        base.copy(logOcr3HeaderSwitchIngredients = "x")
        base.copy(logOcr3HeaderSubheaderDetected = "x")
        base.copy(logOcr3UnfinishedIngrStillEmpty = "x")
        base.copy(logOcr3UnfinishedIngrFused = "x")
        base.copy(logOcr3InvertedIsInverted = "x")
        base.copy(logOcr3InvertedStart = "x")
        base.copy(logOcr3InvertedEnd = "x")
        base.copy(logOcr3InvertedBottomIngrSwitch = "x")
        base.copy(logOcr3NarrativeIsNarrative = "x")
        base.copy(logOcr3NarrativeStart = "x")
        base.copy(logOcr3NarrativeEnd = "x")
        base.copy(logOcr3NarrativeExtractedImpliedIngr = "x")
        base.copy(logOcr3NarrativeExtractedSeasoning = "x")
        base.copy(logOcr3NarrativeSeasoningAlreadyPresent = "x")
        base.copy(logOcr3TitleExtractorStart = "x")
        base.copy(logOcr3TitleExtractorEnd = "x")
        base.copy(logOcr3TitleExtractorRetained = "x")
        base.copy(logOcr3TitleExtractorIngrDetected = "x")
        base.copy(logOcr3TitleExtractorNoIngr = "x")
        base.copy(logOcr3TitleExtractorLineProcessed = "x")
        base.copy(logOcr3TitleExtractorCandidateScore = "x")
        base.copy(logOcr3TitleExtractorNoTitleRetained = "x")
        base.copy(logOcr3TitleExtractorRemainingLine = "x")
        base.copy(logOcr3TitleExtractorSelPoivreCandidate = "x")
        base.copy(logOcr3TitleRepairRuleInvalid = "x")
        base.copy(logOcr3TitleLettrineRepaired = "x")
        base.copy(logOcr3TitleOrphanNounCompleted = "x")
        base.copy(logOcr3TitleNoOrphanNounFound = "x")
        base.copy(logOcr3TitleStripRecipeNumberEmpty = "x")
        base.copy(logOcr3WineDuplicateIgnored = "x")
        base.copy(logOcr3WineEmptyIgnored = "x")
        base.copy(logOcr3WineSectionClosed = "x")
        base.copy(logOcr3WineSectionActive = "x")
        base.copy(logOcr3WineAlreadyClosedIgnored = "x")
        base.copy(logOcr3WineImplicitVolumeClosed = "x")
        base.copy(logOcr3WineImplicitNoVolumeOpen = "x")
        base.copy(logOcr3WineImplicitDuplicateIgnored = "x")
        base.copy(logOcr3WineImplicitEmptyIgnored = "x")
        base.copy(logOcrVocabLoaderCustomDirNoCache = "x")
        base.copy(logErrDiacriticRepairs = "x")
    }

    @Test
    fun fromResources_should_map_all_array_resources_to_vocabulary_lists() {
        val mockRes = mock<Resources>()

        val sampleArray = arrayOf("val1", "val2")
        whenever(mockRes.getStringArray(R.array.step_start_verbs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.step_secondary_verbs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.step_context_words)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.step_action_keywords_complement)).thenReturn(emptyArray<String>())
        whenever(mockRes.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_preparation_modifiers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.instruction_switch_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_semantic_exclusions)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_end_markers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_sub_headers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.source_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.known_source_brands)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.phone_prefixes)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.wine_appellations)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.wine_producers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.wine_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.wine_title_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr3_explicit_wine_headers)).thenReturn(sampleArray)
        whenever(mockRes.getString(R.string.wine_remove_pattern)).thenReturn("pattern")
        whenever(mockRes.getStringArray(R.array.ocr_spelling_repairs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.advertisement_exclusions)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_noise_exclusions)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.title_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_mandatory_complements)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.hanging_connectors)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_variety_qualifiers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.base_ingredients_expecting_variety)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.standalone_articles)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.culinary_measurement_units)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.title_spelling_repairs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.title_multiline_connectors)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_splitter_connectors)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_diacritic_repairs)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.price_title_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.price_categories_low)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.price_categories_medium)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.price_categories_high)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ingredient_connectors)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_protected_section_headers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_time_header_prefixes)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr_time_header_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr1_protected_ingredients_headers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr1_protected_instructions_headers)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.ocr2_technical_dimension_keywords)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.orphan_noun_number_words)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.orphan_noun_stop_words)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.narrative_stop_words)).thenReturn(sampleArray)
        whenever(mockRes.getStringArray(R.array.duration_units)).thenReturn(sampleArray)
        whenever(mockRes.getString(R.string.ocr1_canonical_ingredients_header)).thenReturn("ingrédients")
        whenever(mockRes.getString(R.string.ocr1_canonical_instructions_header)).thenReturn("instructions")
        whenever(mockRes.getString(R.string.ocr1_ingredients_suffix_prefix)).thenReturn("ng")
        whenever(mockRes.getString(R.string.log_ocr1_protected_header_repaired)).thenReturn("Réparation en-tête OCR : '%1\$s' ➔ '%2\$s'")
        whenever(mockRes.getString(R.string.log_ocr1_protected_ingredients_empty)).thenReturn("Attention : ocr1ProtectedIngredientsHeaders est vide dans le vocabulaire")
        whenever(mockRes.getString(R.string.log_ocr1_protected_instructions_empty)).thenReturn("Attention : ocr1ProtectedInstructionsHeaders est vide dans le vocabulaire")
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_source_captured)).thenReturn("CLEANER : Source précoce capturée depuis bannière ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_noise_removed)).thenReturn("CLEANER : Bruit de scan OCR supprimé ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_ad_line_removed)).thenReturn("CLEANER : Ligne publicitaire supprimée ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_ad_prefix_removed)).thenReturn("CLEANER : Préfixe publicitaire effacé de ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_excluded_keyword_removed)).thenReturn("CLEANER : Mot-clé exclu supprimé ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_narrative_delegation)).thenReturn(" => ACTIVER DÉLÉGATION NARRATIVE TYPE 2 (Ocr3NarrativeCategorizer)")
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_start)).thenReturn("--- DÉBUT CATÉGORISATION ---")
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_end)).thenReturn("--- FIN CATÉGORISATION ---")
        whenever(mockRes.getString(R.string.log_ocr3_block_a_metadata_ignored)).thenReturn("  [MÉTADONNÉES / PÉRIODIQUE IGNORÉ] '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_block_a_fusion_continuation)).thenReturn("  [FUSION CONTINUATION] Fusionné : '%1\$s' ➔ Nouvelle ligne : '%2\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_block_c_reappearance_check)).thenReturn("LIGNE: '%1\$s' | Déjà inventorisés: %2\$s")
        whenever(mockRes.getString(R.string.log_ocr3_block_c_reappearance_match)).thenReturn("  ? Comparaison: '%1\$s' -> MATCH=%2\$b | hardQty=%3\$b | weight=%4\$b | closed=%5\$b | final=%6\$b")
        whenever(mockRes.getString(R.string.log_ocr3_block_d_classification_summary)).thenReturn("  => BILAN: reappear=%1\$b | instrSignal=%2\$b | currentSec=%3\$d")
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_ingr_split)).thenReturn("  [INGR] Split: Q=%1\$s, U=%2\$s, N='%3\$s', Info='%4\$s' | Keywords added: %5\$s")
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_instr_added)).thenReturn("  [INSTR] Ligne ajoutée aux instructions ➔ '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_catchup)).thenReturn("  [RATTRAPAGE INGRÉDIENT] En-tête rattrapé depuis section fermée ➔ '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_lock_activated)).thenReturn("  [!] VERROU ACTIVÉ (Marqueur de fin détecté)")
        whenever(mockRes.getString(R.string.log_ocr3_header_switch_instructions)).thenReturn("  [!] BASCULEMENT DE SECTION ➔ INSTRUCTIONS ('%1\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_header_switch_ingredients)).thenReturn("  [!] BASCULEMENT DE SECTION ➔ INGRÉDIENTS ('%1\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_header_subheader_detected)).thenReturn("  [!] SOUS-EN-TÊTE D'INGRÉDIENT DÉTECTÉ ('%1\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_unfinished_ingr_still_empty)).thenReturn("  [FUSION UNITÉ INCOMPLÈTE] Nom d'ingrédient toujours vide après fusion ('%1\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_unfinished_ingr_fused)).thenReturn("  [FUSION UNITÉ INCOMPLÈTE] Fusionné : '%1\$s' ➔ Nouvelle ligne : '%2\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_inverted_is_inverted)).thenReturn("isInvertedRecipe = %1\$b")
        whenever(mockRes.getString(R.string.log_ocr3_inverted_start)).thenReturn("--- DÉBUT CATÉGORISATION INVERSÉE (TYPE 3) ---")
        whenever(mockRes.getString(R.string.log_ocr3_inverted_end)).thenReturn("--- FIN CATÉGORISATION INVERSÉE (TYPE 3) ---")
        whenever(mockRes.getString(R.string.log_ocr3_inverted_bottom_ingr_switch)).thenReturn("  [!] BASCULE SECTION INGRÉDIENTS DU BAS DÉTECTÉE (Ligne : '%1\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_is_narrative)).thenReturn("isNarrativeInstructionOnlyRecipe = %1\$b (première ligne d'instruction='%2\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_start)).thenReturn("--- DÉBUT CATÉGORISATION NARRATIVE (TYPE 2) ---")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_end)).thenReturn("--- FIN CATÉGORISATION NARRATIVE (TYPE 2) ---")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_extracted_implied_ingr)).thenReturn("  [INGRÉDIENT IMPLICITE EXTRAIT] ➔ '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_extracted_seasoning)).thenReturn("  [ASSAISONNEMENT IMPLICITE EXTRAIT] ➔ '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_narrative_seasoning_already_present)).thenReturn("  [ASSAISONNEMENT IMPLICITE] Sel ou poivre déjà présent dans la recette")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_start)).thenReturn("=== DEBUT EXTRACTION DU TITRE PAR SCORING ===")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_end)).thenReturn("=== FIN EXTRACTION DU TITRE PAR SCORING ===")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_retained)).thenReturn(">>> TITRE RETENU INITIALEMENT: '%1\$s' avec SCORE MAX: %2\$d")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_ingr_detected)).thenReturn("TITLE EXTRACTOR : Ingrédients détectés sur la fiche pour scoring ➔ %1\$s")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_no_ingr)).thenReturn("TITLE EXTRACTOR : Aucun mot d'ingrédient détecté pour scoring")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_line_processed)).thenReturn("  [LIGNE TRAITÉE] '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_candidate_score)).thenReturn("TITLE CANDIDATE [#%1\$d]: '%2\$s' ➔ SCORE: %3\$d (%4\$s)")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_no_title_retained)).thenReturn(">>> AUCUN TITRE VALIDE RETENU")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_remaining_line)).thenReturn("  [LIGNE RESTANTE CANDIDATE] '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_sel_poivre_candidate)).thenReturn("TITLE EXTRACTOR : Candidat extrait depuis la ligne sel/poivre ➔ '%1\$s' (score=%2\$d)")
        whenever(mockRes.getString(R.string.log_ocr3_title_repair_rule_invalid)).thenReturn("  [SKIP] Règle de réparation invalide sans séparateur : '%1\$s'")
        whenever(mockRes.getString(R.string.log_ocr3_title_lettrine_repaired)).thenReturn("TITLE EXTRACTOR : Lettrine réparée ➔ '%1\$s' devenant '%2\$s' (via '%3\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_title_orphan_noun_completed)).thenReturn("TITLE EXTRACTOR : Titre complété par nom orphelin ➔ '%1\$s' (ligne consommée : '%2\$s')")
        whenever(mockRes.getString(R.string.log_ocr3_title_no_orphan_noun_found)).thenReturn("  [INFO] Aucun nom orphelin trouvé dans les lignes restantes")
        whenever(mockRes.getString(R.string.log_ocr3_title_strip_recipe_number_empty)).thenReturn("  [INFO] Chaîne vide après suppression du numéro de fiche")
        whenever(mockRes.getString(R.string.log_ocr3_wine_duplicate_ignored)).thenReturn("  [!] Ligne de vin ignorée (doublon déjà enregistré)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_empty_ignored)).thenReturn("  [!] Ligne de vin ignorée (nom vide)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_section_closed)).thenReturn("  [!] SECTION VIN FERMÉE (Volume/Millésime/Enseigne vin détectée)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_section_active)).thenReturn("  [!] Section vin maintenue active (en attente du volume/millésime final)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_already_closed_ignored)).thenReturn("  [!] Ligne de vin ignorée (section vin déjà fermée)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_volume_closed)).thenReturn("  [!] SECTION VIN FERMÉE (Volume/Millésime détecté)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_no_volume_open)).thenReturn("  [!] Vin implicite capturé sans volume (section vin reste ouverte)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_duplicate_ignored)).thenReturn("  [!] Ligne de vin implicite ignorée (doublon déjà enregistré)")
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_empty_ignored)).thenReturn("  [!] Ligne de vin implicite ignorée (nom vide)")
        whenever(mockRes.getString(R.string.log_ocr_vocab_loader_custom_dir_no_cache)).thenReturn("Répertoire personnalisé '%1\$s', non mis en cache")
        whenever(mockRes.getString(R.string.log_err_diacritic_repairs)).thenReturn("Échec du chargement des réparations diacritiques par défaut")

        val vocab = OcrVocabulary.fromResources(mockRes)

        assertWithMessage("Les verbes d'action du vocabulaire chargé par ressources doivent correspondre")
            .that(vocab.stepActionKeywords)
            .isEqualTo(listOf("val1", "val2", "val1", "val2", "val1", "val2"))

        assertWithMessage("Les ingrédients connus doivent correspondre")
            .that(vocab.commonIngredientsNoQty)
            .isEqualTo(listOf("val1", "val2"))

        assertWithMessage("Les mots-clés d'exclusion doivent correspondre")
            .that(vocab.excludedOcrKeywords)
            .isEqualTo(listOf("val1", "val2"))

        assertWithMessage("Les en-têtes d'ingrédients protégés de l'Étape 1 doivent correspondre")
            .that(vocab.ocr1ProtectedIngredientsHeaders)
            .isEqualTo(listOf("val1", "val2"))

        assertWithMessage("Les en-têtes d'instructions protégés de l'Étape 1 doivent correspondre")
            .that(vocab.ocr1ProtectedInstructionsHeaders)
            .isEqualTo(listOf("val1", "val2"))

        assertWithMessage("Le suffixe d'ingrédient doit valoir 'ng'")
            .that(vocab.ocr1IngredientsSuffixPrefix)
            .isEqualTo("ng")

        assertWithMessage("Le motif de suppression de vin doit valoir 'pattern'")
            .that(vocab.wineRemovePattern)
            .isEqualTo("pattern")

        assertWithMessage("Le message d'erreur de chargement diacritique doit correspondre")
            .that(vocab.logErrDiacriticRepairs)
            .isEqualTo("Échec du chargement des réparations diacritiques par défaut")

        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun fromResources_should_handle_missing_resources_safely_without_crashing() {
        val mockRes = mock<Resources>()
        whenever(mockRes.getStringArray(R.array.step_start_verbs)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.wine_remove_pattern)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.ocr1_ingredients_suffix_prefix)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr1_protected_header_repaired)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_source_captured)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_noise_removed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_ad_line_removed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_ad_prefix_removed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr2_cleaner_excluded_keyword_removed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_narrative_delegation)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_start)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_categorizer_end)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_a_metadata_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_c_reappearance_check)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_c_reappearance_match)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_d_classification_summary)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_ingr_split)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_instr_added)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_block_e_dispatch_catchup)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_unfinished_ingr_still_empty)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_unfinished_ingr_fused)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_start)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_end)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_title_extractor_retained)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_duplicate_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_empty_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_section_closed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_section_active)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_already_closed_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_volume_closed)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_no_volume_open)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_duplicate_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr3_wine_implicit_empty_ignored)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_ocr_vocab_loader_custom_dir_no_cache)).doThrow(Resources.NotFoundException())
        whenever(mockRes.getString(R.string.log_err_diacritic_repairs)).doThrow(Resources.NotFoundException())

        val vocab = OcrVocabulary.fromResources(mockRes)

        assertWithMessage("En cas de ressources manquantes, les verbes d'action doivent être une liste vide")
            .that(vocab.stepActionKeywords)
            .isEmpty()

        assertWithMessage("Le motif de suppression de vin doit être une chaîne vide")
            .that(vocab.wineRemovePattern)
            .isEmpty()

        assertWithMessage("Le suffixe d'ingrédient doit être une chaîne vide")
            .that(vocab.ocr1IngredientsSuffixPrefix)
            .isEmpty()

        assertWithMessage("Le message d'erreur de chargement de diacritique doit être vide")
            .that(vocab.logErrDiacriticRepairs)
            .isEmpty()

        OcrVocabularyLoader.clearCache()
    }
}
