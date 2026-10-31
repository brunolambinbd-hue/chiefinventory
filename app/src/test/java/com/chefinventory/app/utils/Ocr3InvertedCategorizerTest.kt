package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'aiguillage et la détection du Type 3 (Inversé).
 */
class Ocr3InvertedCategorizerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInvertedRecipe_EmptyInput_ReturnsFalse() {
        assertThat(Ocr3InvertedCategorizer.isInvertedRecipe(emptyList(), vocabulary)).isFalse()
        assertThat(Ocr3InvertedCategorizer.isInvertedRecipe(listOf("   "), vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeInvertedRecipe_DefaultParametersAndSourceExtraction() {
        val lines = listOf(
            "Tarte aux Fraises",
            "1) Cuire la pâte à blanc pendant 15 minutes.",
            "POUR 4 PERSONNES",
            "250 g de fraises",
            "Hôtel Hilton",
            "Bordeaux 75cl"
        )

        val result = Ocr3InvertedCategorizer.categorizeInvertedRecipe(lines, vocabulary)
        assertThat(result.rawIngredientsList).contains("250 g de fraises")
        assertThat(result.detectedSourceList).contains("Hôtel Hilton")
        assertThat(result.detectedWineList).contains("Bordeaux 75cl")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeInvertedRecipe_TwoLineWineHeader_WorkingScenario() {
        val lines = listOf(
            "1. Mélanger la pâte.",
            "4 portions",
            "200 g de sucre",
            "Vin conseillé :",
            "Pinot Noir 75cl"
        )

        val result = Ocr3InvertedCategorizer.categorizeInvertedRecipe(lines, vocabulary)
        assertThat(result.rawIngredientsList).contains("200 g de sucre")
        assertThat(result.detectedWineList).contains("Pinot Noir 75cl")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSingleLineWineHeaderDropBug_ReproducesFailure() {
        val lines = listOf(
            "1. Mélanger la farine avec les oeufs.",
            "POUR 4 PERSONNES",
            "200 g de farine",
            "3 oeufs",
            "Vin conseillé : Bordeaux 75cl"
        )

        val result = Ocr3InvertedCategorizer.categorizeInvertedRecipe(lines, vocabulary)

        // Single-line wine recommendation "Vin conseillé : Bordeaux 75cl"
        assertThat(result.detectedWineList).contains("Bordeaux 75cl")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeInvertedRecipe_SectionResetOnHeader() {
        val lines = listOf(
            "• Couper les pommes.",
            "POUR 4 PERSONNES",
            "3 pommes",
            "COMPOSITION :",
            "100 g de sucre"
        )

        val result = Ocr3InvertedCategorizer.categorizeInvertedRecipe(lines, vocabulary)
        assertThat(result.rawIngredientsList).contains("3 pommes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInvertedRecipe_ChitsDeConcombre() {
        val ocrText = """
            Chits de concombre au: eus de truite
            1) Découpez deux grands Çons de 1/2 cm dans le concombre.
            2) Pendant ce temps, préparez la farce.
            3) Farcissez les puits de concombre.
            4) Décorez les assiettes.
            POUR 4 PERSONNES
            1 grand concombre
            1 petit pot d'œufs de truite (50 g)
            250 g de fromage blanc
            1 c. à soupe de cerfeuil haché
            1 c. à soupe de ciboulette hachée
            1 c. à soupe d'aneth haché
            1 c. à soupe d'oignons frais hachés
            quelques feuilles de salade
            1 tomate
            sel et poivre
            20 mn
            BOURGOGNE CHARDONNAY Michel Laroche
        """.trimIndent()

        val isInv = Ocr3InvertedCategorizer.isInvertedRecipe(ocrText.lines(), vocabulary)
        assertThat(isInv).isTrue()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        assertThat(result.title).ignoringCase().contains("Puits de concombre aux œufs de truite")
        assertThat(result.ingredients).ignoringCase().contains("1 grand concombre")
        assertThat(result.instructions).ignoringCase().contains("Découpez deux grands")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInvertedRecipe_ReturnsFalseForExplicitHeaders() {
        val lines1 = listOf(
            "INGRÉDIENTS",
            "200 g de farine",
            "100 g de beurre"
        )
        assertThat(Ocr3InvertedCategorizer.isInvertedRecipe(lines1, vocabulary)).isFalse()

        val lines2 = listOf(
            "COMPOSITION",
            "200 g de farine",
            "100 g de beurre"
        )
        assertThat(Ocr3InvertedCategorizer.isInvertedRecipe(lines2, vocabulary)).isFalse()
    }
}
