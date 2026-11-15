package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau A (Méta-données & Nettoyage) vérifiant l'extraction
 * du suffixe calorique '233 KCALPOR.' et la purge intégrale de 'POR.' sur la recette Lasagne au Saumon.
 */
class OcrLasagneSaumonMetadataTest {

    private val rawOcrScanText = listOf(
        "LASAGNE AU SAUMON,",
        "RICOTTA ET ÉPINARDS",
        "O 25 MIN +30 MIN DE CUISSON (+ REFROIDISSEMENT) FACILE",
        "4 PERS. 233 KCALPOR.",
        "INGRÉDIENTS",
        "250 g de lasagnes",
        "300 g de saumon",
        "INSTRUCTIONS",
        "• Préparez les lasagnes."
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
    fun testLasagneSaumon_ExtractsKcalPor_AndPurgesPorFromResidualText() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        assertWithMessage("Le temps de préparation doit être '25'")
            .that(result.prepTime)
            .isEqualTo("25")

        assertWithMessage("Le temps de cuisson doit être '30'")
            .that(result.cookTime)
            .isEqualTo("30")

        assertWithMessage("L'apport calorique extrait doit être '233'")
            .that(result.kcalPerServing)
            .isEqualTo("233")

        val instructions = result.instructions.orEmpty()
        assertWithMessage("Les instructions ne doivent PAS contenir le mot parasite 'REFROIDISSEMENT'")
            .that(instructions)
            .doesNotContain("REFROIDISSEMENT")

        assertWithMessage("Les instructions ne doivent PAS contenir le mot parasite 'POR'")
            .that(instructions)
            .doesNotContain("POR")
    }
}
