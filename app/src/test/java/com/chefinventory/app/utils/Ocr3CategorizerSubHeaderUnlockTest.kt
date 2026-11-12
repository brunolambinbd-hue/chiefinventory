package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau B (Étape 3) vérifiant que la détection d'un sous-en-tête d'ingrédients
 * avec deux-points ("Pour la sauce :") déverrouille la section d'ingrédients fermée après "sel et poivre"
 * et conserve les lignes d'ingrédients ("4 c. à soupe de jus d'orange") et modificateurs ("fraîche hachée")
 * dans les ingrédients bruts.
 */
class Ocr3CategorizerSubHeaderUnlockTest {

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
    fun testCategorize_SubHeaderWithColonAfterSaltPepper_UnlocksSectionAndKeepsModifiersInIngredients() {
        val rawLines = listOf(
            "INGRÉDIENTS",
            "300 g de crevettes roses",
            "sel et poivre",
            "Pour la sauce :",
            "4 c. à soupe de jus d'orange",
            "1 c. à soupe de coriandre",
            "fraîche hachée",
            "INSTRUCTIONS",
            "• Mélangez les ingrédients."
        )

        val result = Ocr3Categorizer.categorize(rawLines, vocabulary)

        assertWithMessage("La section ingrédients doit contenir la ligne 'Pour la sauce :'")
            .that(result.rawIngredientsList)
            .contains("Pour la sauce :")

        assertWithMessage("La section ingrédients déverrouillée doit contenir '4 c. à soupe de jus d'orange'")
            .that(result.rawIngredientsList)
            .contains("4 c. à soupe de jus d'orange")

        assertWithMessage("La section ingrédients déverrouillée doit contenir '1 c. à soupe de coriandre'")
            .that(result.rawIngredientsList)
            .contains("1 c. à soupe de coriandre")

        assertWithMessage("La section ingrédients déverrouillée doit contenir 'fraîche hachée'")
            .that(result.rawIngredientsList)
            .contains("fraîche hachée")

        assertWithMessage("Le modificateur 'fraîche hachée' ne doit PAS fuiter dans les instructions brutes")
            .that(result.rawInstructionsList)
            .doesNotContain("fraîche hachée")
    }
}
