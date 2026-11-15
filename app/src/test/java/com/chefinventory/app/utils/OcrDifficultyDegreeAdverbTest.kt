package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau A (Méta-données & Nettoyage) vérifiant l'extraction
 * des expressions de difficulté avec adverbes de degré ('ASSEZ FACILE', 'TRÈS FACILE') et la purge
 * intégrale de l'adverbe pour éviter sa fuite dans le texte.
 */
class OcrDifficultyDegreeAdverbTest {

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
    fun testExtractDifficulty_AssezFacile_ExtractsFacileAndPurgesAssez() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawOcrText = """
            TORTILLAS DE QUINOA
            15 MIN DE PRÉPARATION + 15 MIN DE CUISSON ASSEZ FACILE
            INGRÉDIENTS
            100 g de quinoa
            INSTRUCTIONS
            • Préparez la pâte.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("La difficulté extraite doit être normalisée en 'facile'")
            .that(result.difficulty)
            .ignoringCase()
            .isEqualTo("facile")

        val instructions = result.instructions.orEmpty()
        assertWithMessage("Les instructions ne doivent PAS contenir le mot orphelin 'ASSEZ'")
            .that(instructions)
            .doesNotContain("ASSEZ")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractDifficulty_TresFacile_ExtractsFacileAndPurgesTres() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawOcrText = """
            SALADE DE TOMATES
            10 MIN TRÈS FACILE
            INGRÉDIENTS
            4 tomates
            INSTRUCTIONS
            • Coupez les tomates.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("La difficulté extraite pour 'TRÈS FACILE' doit être 'facile'")
            .that(result.difficulty)
            .ignoringCase()
            .isEqualTo("facile")

        val instructions = result.instructions.orEmpty()
        assertWithMessage("Les instructions ne doivent PAS contenir le mot orphelin 'TRÈS'")
            .that(instructions)
            .doesNotContain("TRÈS")
    }
}
