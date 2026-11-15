package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation (Correction 2) vérifiant que la scorie parasite 'S3' est purvée
 * en Étape 1 sans supprimer la ligne d'ingrédient de l'ail ('• 2 gousses d'ail S3' -> '2 gousses d'ail').
 */
class OcrAilS3NoiseCleaningTest {

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
    fun testAilS3Scoria_PurgesS3AndKeepsGarlicIngredient() {
        val rawOcrText = """
            SOUPE AU CÉLERI-RAVE
            INGRÉDIENTS
            •h céleri-rave
            • 2 oignons
            • 2 gousses d'ail S3
            • 4 brins de thym
            INSTRUCTIONS
            • Épluchez le céleri-rave.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)
        val ingredients = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("L'ingrédient '2 gousses d'ail' doit figurer dans les ingrédients")
            .that(ingredients)
            .contains("2 gousses d'ail")

        assertWithMessage("La scorie 'S3' doit être 100% purgée")
            .that(result.ingredients.orEmpty())
            .doesNotContain("S3")
    }
}
