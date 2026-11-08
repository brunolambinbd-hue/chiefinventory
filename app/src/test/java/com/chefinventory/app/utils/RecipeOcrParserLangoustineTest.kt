package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'intégration anti-régression dédiée à la recette des Salades de Langoustines au Curry.
 *
 * Cette classe sert de modèle de référence pour valider l'extraction complète des 5 étapes du pipeline OCR :
 * - Fusion des ingrédients et des instructions (Étape 4)
 * - Extraction du titre avec lettrine réparée (Étape 3)
 * - Extraction des portions, du vin conseillé et des métadonnées (Étape 3 & 6)
 * - Formatage et majusculation finale (Étape 5)
 */
class RecipeOcrParserLangoustineTest {

    /**
     * Reconfiguration propre du cache de vocabulaire avant chaque test.
     */
    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    /**
     * Nettoyage du cache de vocabulaire après chaque test pour éviter toute pollution mémoire inter-classes.
     */
    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    /**
     * Comparateur de listes de chaînes avec affichage comparatif indexé par ligne en cas de divergence (en Anglais Développeur).
     *
     * @param actual La liste des lignes réelles produites par le pipeline OCR.
     * @param expected La liste des lignes attendues.
     * @param description Le libellé neutre de la vérification.
     */
    private fun assertLinesMatch(actual: List<String>, expected: List<String>, description: String) {
        if (actual != expected) {
            val message = "$description :\n\n" +
                "${OcrTestStrings.HEADER_EXPECTED} (${expected.size} lines) :\n" +
                expected.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n") + "\n\n" +
                "${OcrTestStrings.HEADER_ACTUAL} (${actual.size} lines) :\n" +
                actual.mapIndexed { i, s -> "  [$i] $s" }.joinToString("\n")
            fail(message)
        }
    }

    /**
     * Valide spécifiquement la fusion des lignes d'ingrédients brutes isolées lors de l'Étape 4 ([Ocr4Merger]).
     */
    @Test
    fun testLangoustineRecipeIngredientsMerge() {
        val rawIngredients = listOf(
            "24 langoustines",
            "cuites",
            "1 mangue",
            "à",
            "point",
            "4 c. à",
            "soupe",
            "de",
            "petits",
            "croûtons",
            "3 c. à",
            "soupe",
            "de",
            "mayonnaise",
            "1 c. à",
            "café",
            "de",
            "curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles",
            "feuilles",
            "de",
            "salade",
            "cresson",
            "sel",
            "et",
            "poivre"
        )

        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val merged = Ocr4Merger.mergeIngredients(
            rawIngredients,
            vocabulary.ingredientPreparationModifiers,
            vocabulary.commonIngredientsNoQty,
            vocabulary.mandatoryComplementIngredients,
            vocabulary.ingredientConnectors
        )

        val expectedIngredients = listOf(
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson",
            "sel et poivre"
        )

        assertLinesMatch(merged, expectedIngredients, OcrTestStrings.STEP4_MERGE_MISMATCH)
    }

    /**
     * Valide le parsing complet End-to-End à partir de la capture brute réelle ML Kit de la caméra d'un scan.
     */
    @Test
    fun testLangoustineRecipeExactOcrRawScanParse() {
        val exactOcrRawText = listOf(
            "r",
            "e",
            "e t",
            "Ingrédients pour 4 personnes",
            "24 langoustines cuites",
            "I mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "lc. à café de curry, I/2 citron",
            "I yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "cresson, sel et poivre",
            "e t",
            "126",
            "alade de langoustines au curry",
            "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et",
            "un trait de jus de citron. Salez et poivrez, réservez.",
            "Décortiquez les langoustines. Réservez 4 belles têtes.",
            "Pelez la mangue, taillez-la en fines lamelles. Lavez et essorez la sala-",
            "de; lavez le cresson.",
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des",
            "lamelles de mangue. Décorez de salade et de cresson, ajoutez les",
            "croûtons.",
            "Disposez une tête de langoustine sur chaque assiette, nappez les lan-",
            "goustines d'un peu de sauce au curry.",
            "Servez frais, en présentant le restant de la sauce à part.",
            "Vous poOuvez préparer une soupe avec les têtes et les carcasses des lan-",
            "goustines.",
            "ExprrtEANS",
            "Vin conseillé",
            "Gewurztraminer,",
            "Caves de Pfaffenhein",
            "MO, 1992, 75 cl"
        ).joinToString("\n")

        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(exactOcrRawText, vocabulary)

        // 1. Validation du Titre
        assertWithMessage(OcrTestStrings.TITLE_MISMATCH)
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Salade de langoustines au curry")

        // 2. Validation des Portions / Nombre de Personnes
        assertWithMessage(OcrTestStrings.SERVINGS_MISMATCH)
            .that(result.servings)
            .isEqualTo("4")

        // 3. Validation du Vin Conseillé
        assertWithMessage(OcrTestStrings.WINE_VARIETAL_MISMATCH)
            .that(result.wine)
            .contains("Gewurztraminer")

        assertWithMessage(OcrTestStrings.WINE_PRODUCER_MISMATCH)
            .that(result.wine)
            .contains("Caves de Pfaffenhein")

        assertWithMessage(OcrTestStrings.WINE_VINTAGE_MISMATCH)
            .that(result.wine)
            .contains("1992")

        assertWithMessage(OcrTestStrings.WINE_VOLUME_MISMATCH)
            .that(result.wine)
            .contains("75 cl")

        // 4. Validation des Champs Sans Données (Modèle Neutre)
        assertWithMessage(OcrTestStrings.SOURCE_MISMATCH)
            .that(result.source)
            .isNull()

        assertWithMessage(OcrTestStrings.PREP_TIME_MISMATCH)
            .that(result.prepTime)
            .isNull()

        assertWithMessage(OcrTestStrings.COOK_TIME_MISMATCH)
            .that(result.cookTime)
            .isNull()

        assertWithMessage(OcrTestStrings.RESTING_TIME_MISMATCH)
            .that(result.restingTime)
            .isNull()

        assertWithMessage(OcrTestStrings.KCAL_MISMATCH)
            .that(result.kcalPerServing)
            .isNull()

        assertWithMessage(OcrTestStrings.DIFFICULTY_MISMATCH)
            .that(result.difficulty)
            .isNull()

        // 5. Validation des Ingrédients
        val ingredientLines = result.ingredients?.split("\n") ?: emptyList()
        val expectedIngredients = listOf(
            "24 langoustines cuites",
            "1 mangue à point",
            "4 c. à soupe de petits croûtons",
            "3 c. à soupe de mayonnaise",
            "1 c. à café de curry",
            "1/2 citron",
            "1 yaourt nature (125 g)",
            "4 belles feuilles de salade",
            "Cresson",
            "Sel et poivre"
        )
        assertLinesMatch(ingredientLines, expectedIngredients, OcrTestStrings.INGREDIENTS_MISMATCH)

        // 6. Validation des Instructions
        val instructionLines = result.instructions?.split("\n") ?: emptyList()
        val expectedInstructions = listOf(
            "Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et un trait de jus de citron.",
            "Salez et poivrez, réservez.",
            "Décortiquez les langoustines.",
            "Réservez 4 belles têtes.",
            "Pelez la mangue, taillez-la en fines lamelles.",
            "Lavez et essorez la salade; lavez le cresson.",
            "Disposez six langoustines sur chaque assiette, ajoutez tout autour des lamelles de mangue.",
            "Décorez de salade et de cresson, ajoutez les croûtons.",
            "Disposez une tête de langoustine sur chaque assiette, nappez les langoustines d'un peu de sauce au curry.",
            "Servez frais, en présentant le restant de la sauce à part.",
            "Vous pouvez préparer une soupe avec les têtes et les carcasses des langoustines."
        )
        assertLinesMatch(instructionLines, expectedInstructions, OcrTestStrings.INSTRUCTIONS_MISMATCH)
    }
}
