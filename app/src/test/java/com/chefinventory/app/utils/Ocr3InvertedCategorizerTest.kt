package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Une liste de lignes vide ne doit pas être qualifiée de recette inversée")
            .that(Ocr3InvertedCategorizer.isInvertedRecipe(emptyList(), vocabulary))
            .isFalse()

        assertWithMessage("Une liste contenant uniquement un espace ne doit pas être qualifiée de recette inversée")
            .that(Ocr3InvertedCategorizer.isInvertedRecipe(listOf("   "), vocabulary))
            .isFalse()
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
        assertWithMessage("Les ingrédients de la recette inversée doivent contenir '250 g de fraises'")
            .that(result.rawIngredientsList)
            .contains("250 g de fraises")

        assertWithMessage("La source d'établissement doit être capturée ('Hôtel Hilton')")
            .that(result.detectedSourceList)
            .contains("Hôtel Hilton")

        assertWithMessage("Le vin doit être capturé ('Bordeaux 75cl')")
            .that(result.detectedWineList)
            .contains("Bordeaux 75cl")
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
        assertWithMessage("Les ingrédients doivent contenir '200 g de sucre'")
            .that(result.rawIngredientsList)
            .contains("200 g de sucre")

        assertWithMessage("Le vin multiligne 'Pinot Noir 75cl' sous en-tête doit être capturé")
            .that(result.detectedWineList)
            .contains("Pinot Noir 75cl")
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

        assertWithMessage("La recommandation de vin mono-ligne 'Vin conseillé : Bordeaux 75cl' doit être capturée")
            .that(result.detectedWineList)
            .contains("Bordeaux 75cl")
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
        assertWithMessage("Les ingrédients de la recette inversée doivent contenir '3 pommes'")
            .that(result.rawIngredientsList)
            .contains("3 pommes")
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
        assertWithMessage("La recette 'Puits de concombre' avec instructions en premier doit être détectée comme inversée")
            .that(isInv)
            .isTrue()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        assertWithMessage("Le titre de la recette inversée doit être réparé en 'Puits de concombre aux œufs de truite'")
            .that(result.title)
            .ignoringCase()
            .contains("Puits de concombre aux œufs de truite")

        assertWithMessage("Les ingrédients doivent contenir '1 grand concombre'")
            .that(result.ingredients)
            .ignoringCase()
            .contains("1 grand concombre")

        assertWithMessage("Les instructions doivent contenir la première étape 'Découpez deux grands'")
            .that(result.instructions)
            .ignoringCase()
            .contains("Découpez deux grands")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInvertedRecipe_ReturnsFalseForExplicitHeaders() {
        val lines1 = listOf(
            "INGRÉDIENTS",
            "200 g de farine",
            "100 g de beurre"
        )
        assertWithMessage("Une fiche avec l'en-tête explicite 'INGRÉDIENTS' ne doit pas être qualifiée d'inversée")
            .that(Ocr3InvertedCategorizer.isInvertedRecipe(lines1, vocabulary))
            .isFalse()

        val lines2 = listOf(
            "COMPOSITION",
            "200 g de farine",
            "100 g de beurre"
        )
        assertWithMessage("Une fiche avec l'en-tête explicite 'COMPOSITION' ne doit pas être qualifiée d'inversée")
            .that(Ocr3InvertedCategorizer.isInvertedRecipe(lines2, vocabulary))
            .isFalse()
    }
}
