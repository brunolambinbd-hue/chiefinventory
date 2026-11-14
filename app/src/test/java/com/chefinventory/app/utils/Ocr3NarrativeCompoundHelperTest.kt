package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la détection des fragments de phrases d'instructions narratives
 * composées par l'assistant [Ocr3NarrativeCompoundHelper].
 */
class Ocr3NarrativeCompoundHelperTest {

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
    fun testIsNarrativeCompoundInstruction_NarrativeSentenceFragments_ReturnsTrue() {
        val line1 = "de jus d'orange et une pincée de sel."
        val line2 = "d'un jus de citron et d'une pincée de poivre."
        val line3 = "de 1 cuillère de miel et d'une pincée de sel."

        assertWithMessage("Le fragment 'de jus d'orange et une pincée de sel.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line1))
            .isTrue()

        assertWithMessage("Le fragment 'd'un jus de citron et d'une pincée de poivre.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line2))
            .isTrue()

        assertWithMessage("Le fragment 'de 1 cuillère de miel et d'une pincée de sel.' doit être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line3))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeCompoundInstruction_ValidStandaloneIngredients_ReturnsFalse() {
        val line1 = "1 pincée de sel"
        val line2 = "200 g de farine"
        val line3 = "4 c. à soupe de jus d'orange"

        assertWithMessage("L'ingrédient '1 pincée de sel' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line1))
            .isFalse()

        assertWithMessage("L'ingrédient '200 g de farine' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line2))
            .isFalse()

        assertWithMessage("L'ingrédient '4 c. à soupe de jus d'orange' ne doit PAS être détecté comme instruction narrative")
            .that(Ocr3NarrativeCompoundHelper.isNarrativeCompoundInstruction(line3))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_SaladeAvocatsOrangeRecipeScan_KeepsFragmentInInstructions() {
        val rawOcrText = """
            Salade d'avocats aux oranges d'Espagne
            INGRÉDIENTS
            2 avocats bien mûrs
            6 oranges
            10 feuilles de laitue
            1 c. à café de menthe fraîche hachée
            2 c. à soupe de graines de cumin
            1 c. à soupe de miel
            1 c. à café d'huile d'olive
            1 pincée de poivre de Cayenne
            sel
            INSTRUCTIONS
            • Dans un bol-mélangeur, chiffonnez la laitue avec l'huile d'olive, un peu
            de jus d'orange et une pincée de sel.
            • Distribuez cette salade dans 4 assiettes creuses.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val ing = parsed.ingredients.orEmpty()
        val inst = parsed.instructions.orEmpty()

        assertWithMessage("Les ingrédients ne doivent PAS contenir le fragment 'de jus d'orange et une pincée de sel.'")
            .that(ing)
            .doesNotContain("de jus d'orange")

        assertWithMessage("Les instructions doivent contenir la phrase complète de préparation")
            .that(inst)
            .contains("Dans un bol-mélangeur, chiffonnez la laitue avec l'huile d'olive, un peu de jus d'orange et une pincée de sel.")
    }
}
