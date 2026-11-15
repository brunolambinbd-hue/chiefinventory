package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau A (Méta-données & Nettoyage) vérifiant l'extraction
 * des durées combinées avec artefacts OCR ('O25 MIN + 15 MIN DE CUISSON'), de la difficulté et la purge intégrale.
 */
class OcrTortillasQuinoaMetadataTest {

    private val rawOcrScanText = listOf(
        "TORTILLAS DE QUINOA",
        "AUX ÉPINARDS",
        "O25 MIN +15 MIN DE CUISSON ASSEZ FACILE OEEE",
        "A2PERS, O745 KCAL/POR.",
        "NUTR-SCoRE",
        "B",
        "INGRÉDIENTS",
        "100 g de quinoa",
        "100 g d'épinards",
        "INSTRUCTIONS",
        "• Préparez les tortillas."
    ).joinToString("\n")

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTortillasQuinoa_ExtractsCombinedTimes_AndPurgesResidualText() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        assertWithMessage("Le temps de préparation extrait doit être '25'")
            .that(result.prepTime)
            .isEqualTo("25")

        assertWithMessage("Le temps de cuisson extrait doit être '15'")
            .that(result.cookTime)
            .isEqualTo("15")

        assertWithMessage("La difficulté extraite doit être normalisée en 'facile'")
            .that(result.difficulty)
            .ignoringCase()
            .isEqualTo("facile")

        val instructions = result.instructions.orEmpty()
        assertWithMessage("Les instructions ne doivent PAS contenir le temps résiduel 'O25 MIN'")
            .that(instructions)
            .doesNotContain("O25 MIN")
    }
}
