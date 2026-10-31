package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

/**
 * Modèle de domaine représentant l'ensemble du vocabulaire, des mots-clés et des règles du pipeline OCR.
 *
 * Cette classe est une structure de données Kotlin pure, indépendante du Framework Android,
 * permettant au pipeline OCR d'être rapide, déterministe et testable sans mocks.
 *
 * ---
 * ### 📘 GUIDE DÉVELOPPEUR : PROCÉDURE D'EXTENSION DU DICTIONNAIRE OCR
 *
 * #### 1. Ajout d'un nouvel élément dans un dictionnaire existant :
 * 1. Ouvrir le fichier XML cible dans `app/src/main/res/values/` (ex: `ocr_shared_ingredients.xml`, `ocr_step1_spelling_repairs.xml`).
 * 2. Ajouter la nouvelle entrée sous la balise `<string-array>` ou `<string>`.
 *    - *Note pour les dictionnaires de réparation :* Respecter scrupuleusement le format `"fautif|correct"` (ex: `<item>càs|c. à soupe</item>`).
 * 3. **Aucune modification Kotlin requise !** `OcrVocabularyLoader` lira automatiquement la nouvelle entrée.
 *
 * #### 2. Ajout d'une nouvelle catégorie / `<string-array>` dans un XML existant :
 * 1. Déclarer le tableau dans le fichier XML : `<string-array name="mon_nouveau_tableau"><item>valeur</item></string-array>`.
 * 2. Ajouter la propriété dans [OcrVocabulary] : `val monNouveauTableau: List<String> = emptyList()`.
 * 3. Binder le chargement Android dans [OcrVocabulary.fromResources] :
 *    `monNouveauTableau = getStringArraySafely(res, R.array.mon_nouveau_tableau)`
 * 4. Binder le chargement JVM/TDD dans [OcrVocabularyLoader.loadFromProjectResources] :
 *    `monNouveauTableau = stringArrays["mon_nouveau_tableau"].orEmpty()`
 * 5. Ajouter l'assertion dans `OcrVocabularyLoaderTest.testExhaustiveProductionXmlResourcesIntegrity_All38PropertiesLoadedAndValid` :
 *    `assertThat(vocab.monNouveauTableau).isNotEmpty()`
 *
 * #### 3. Ajout d'un nouveau fichier XML de ressources (ex: `ocr_step6_custom_rules.xml`) :
 * 1. Créer le fichier XML dans `app/src/main/res/values/ocr_step6_custom_rules.xml`.
 * 2. Définir vos balises `<string-array name="...">` ou `<string name="...">`.
 * 3. Suivre la procédure du **Point 2** ci-dessus.
 *    - *Note :* `OcrVocabularyLoader` scanne automatiquement tout le répertoire `res/values/`, il n'y a aucun chemin de fichier à enregistrer manuellement.
 *
 * #### 4. Mise à jour des Fichiers de Tests Unitaires :
 * 1. **Test d'Intégrité des Ressources XML** :
 *    - Si vous avez ajouté une nouvelle propriété à [OcrVocabulary], ajouter l'assertion dans `OcrVocabularyLoaderTest.testExhaustiveProductionXmlResourcesIntegrity_All38PropertiesLoadedAndValid` :
 *      `assertThat(vocab.monNouveauTableau).isNotEmpty()`
 * 2. **Tests de Régrésion d'Analyse OCR** :
 *    - Si vous avez ajouté une règle de correction/remplacement orthographique ou de diacritiques,
 *      ajouter un cas de test dédié dans le fichier de test du composant concerné (ex: `Ocr1NormalizerTest`, `Ocr4MergerTest`, `Ocr5BeautifierTest`).
 * 3. **Scénarios Fiches Recettes** :
 *    - Si l'ajout cible une typologie de recette spécifique (ex: salade, gâteau, vin),
 *      enrichir la suite de tests de scénarios réels (ex: `RecipeOcrParserTest`).
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
) {
    companion object {
        /**
         * Factory method to load an [OcrVocabulary] instance from Android [Resources].
         * This bridges the Android Framework layer with the domain layer.
         */
        fun fromResources(res: Resources): OcrVocabulary {
            return OcrVocabulary(
                stepActionKeywords = getStringArraySafely(res, R.array.step_action_keywords),
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
            )
        }

        private fun getStringArraySafely(res: Resources, id: Int): List<String> {
            return try {
                res.getStringArray(id).toList()
            } catch (_: Exception) {
                emptyList()
            }
        }

        private fun getStringSafely(res: Resources, id: Int): String {
            return try {
                res.getString(id)
            } catch (_: Exception) {
                ""
            }
        }
    }
}
