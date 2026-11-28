package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerOneColumnOnlyInstructions] (Lecteur 3 : 1 colonne, instructions seules).
 */
class Ocr3CategorizerOneColumnOnlyInstructionsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOneColumnOnlyInstructions_ExtractsImpliedIngredientsForPantry() {
        val lines = listOf(
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile d'olive pendant 10 min.",
            "Servez bien chaud."
        )

        val isNarrative = Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
        assertThat(isNarrative).isTrue()

        val sections = Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(lines, vocabulary)
        assertThat(sections.rawInstructionsList).hasSize(2)
        assertThat(sections.rawIngredientsList).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_ExhaustiveBranchCoverage() {
        assertWithMessage("Ocr3CategorizerOneColumnOnlyInstructions doit générer une représentation textuelle valide")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.toString())
            .contains("Ocr3CategorizerOneColumnOnlyInstructions")

        // 1. Empty lines -> false
        assertWithMessage("Une liste de lignes vide ne doit pas être qualifiée de recette narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(emptyList(), vocabulary))
            .isFalse()

        // 2. Explicit ingredients header -> false
        val linesWithIngrHeader = listOf("INGRÉDIENTS", "200 g de poulet", "Faites revenir le poulet")
        assertWithMessage("Une fiche avec en-tête d'ingrédients explicite ne doit pas être narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader, vocabulary))
            .isFalse()

        // 3. Instruction header with quantified ingredient -> false (Type 1)
        val linesWithInstAndIngr = listOf("INSTRUCTIONS", "200 g de poulet", "Faites revenir le poulet")
        assertWithMessage("Une fiche avec en-tête d'instructions et ingrédient quantifié doit retourner faux")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithInstAndIngr, vocabulary))
            .isFalse()

        // 4. Bottom ingredient list ("POUR 4 PERSONNES") -> false (Type 4)
        val linesWithBottomList = listOf("Soupe de légumes", "Faites cuire les légumes", "POUR 4 PERSONNES", "200 g de carottes")
        assertWithMessage("Une fiche avec liste d'ingrédients du bas ne doit pas être narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithBottomList, vocabulary))
            .isFalse()

        // 4b. Bottom ingredient list separated ("pour" + "4 personnes") -> false
        val linesWithBottomListSeparated = listOf("Soupe de légumes", "Faites cuire les légumes", "pour", "4 personnes", "200 g de carottes")
        assertWithMessage("Une fiche avec 'pour' puis 'personnes' en bas ne doit pas être narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithBottomListSeparated, vocabulary))
            .isFalse()

        // 5. Standalone ingredient line before instructions -> false
        val linesWithStandaloneIngr = listOf("200 g de poulet", "Faites revenir le poulet")
        assertWithMessage("Une fiche avec ingrédient autonome avant les instructions ne doit pas être narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithStandaloneIngr, vocabulary))
            .isFalse()

        // 6. Nutrition table line ignored
        val linesWithNutrition = listOf("valeurs nutritionnelles", "protéines 20g", "1. Faites revenir 200 g de poulet", "2) Servez chaud")
        assertWithMessage("Une ligne de tableau nutritionnel ne doit pas bloquer la détection narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithNutrition, vocabulary))
            .isTrue()

        // 7. Bullet / step number instructions -> true
        val linesWithBullets = listOf("• Pelez le melon", "- Coupez en tranches", "3) Servez frais")
        assertWithMessage("Une fiche avec puces et numéros d'étape doit être qualifiée de narrative")
            .that(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesWithBullets, vocabulary))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeNarrativeRecipe_ExhaustiveBranchCoverage() {
        val lines = listOf(
            "   ",
            "Préparation : 15 min",
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile.",
            "Par le chef Yilmaz",
            "Notre vin conseillé : Bordeaux 1996 75 cl",
            "Servez bien chaud."
        )

        val sections = Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(lines, vocabulary)

        assertWithMessage("Le temps de préparation '15' doit être extrait des méta-données")
            .that(sections.detectedPrepTime)
            .isEqualTo("15")

        assertWithMessage("La recommandation de vin 'Bordeaux 1996 75 cl' doit être capturée")
            .that(sections.detectedWineList)
            .contains("Bordeaux 1996 75 cl")

        assertWithMessage("La source 'Par le chef Yilmaz' doit être capturée")
            .that(sections.detectedSourceList)
            .contains("Par le chef Yilmaz")

        assertWithMessage("Les consignes fluides 'Faites revenir...' et 'Servez...' doivent figurer dans rawInstructionsList")
            .that(sections.rawInstructionsList)
            .containsAtLeast(
                "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile.",
                "Servez bien chaud."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeAndCategorize_ExhaustiveBranchCoverage() {
        val customVocab = vocabulary.copy(
            timeHeaderKeywords = listOf("min"),
            excludedOcrKeywords = listOf("Bruit")
        )

        // 1. Instruction header with quantified ingredient -> returns false (Type 1 recipe)
        val linesType1 = listOf("INSTRUCTIONS", "200 g de farine", "Faites un puit et mélanger")
        assertThat(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesType1, customVocab)).isFalse()

        // 2. Nutrition table line in bottom list check
        val linesNutritionBottom = listOf("Ligne 1", "Ligne 2", "Valeurs nutritionnelles", "pour 4 personnes")
        assertThat(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesNutritionBottom, customVocab)).isFalse()

        // 3. Excluded keyword in instruction line check
        val linesExcluded = listOf("pour", "Bruit", "Mélanger")
        assertThat(Ocr3CategorizerOneColumnOnlyInstructions.isNarrativeInstructionOnlyRecipe(linesExcluded, customVocab)).isTrue()

        // 4. Duplicate source & noise line after wine section closed in categorizeNarrativeRecipe
        val linesCat = listOf("Notre vin conseillé : Bordeaux 1996 75 cl", "Bruit", "Par le chef Yilmaz", "Par le chef Yilmaz", "Mélanger")
        val result = Ocr3CategorizerOneColumnOnlyInstructions.categorizeNarrativeRecipe(linesCat, customVocab)
        assertThat(result.detectedSourceList).containsExactly("Par le chef Yilmaz")
    }
}
