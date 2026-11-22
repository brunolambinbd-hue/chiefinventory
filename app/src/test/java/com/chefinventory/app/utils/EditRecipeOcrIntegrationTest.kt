package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test d'intégration TDD d'isolation certifiant la transmission à chargement unique de la taxonomie OFF de Room DB à [RecipeOcrParser].
 */
class EditRecipeOcrIntegrationTest {

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
    fun testParse_WithPreloadedOffTaxonomyList_FixesElisionsInInstructions() {
        val ocrText = """
            Salade océane
            INGRÉDIENTS
            • 250 g de crevettes grises
            • 1 orange
            INSTRUCTIONS
            • Prélevez finement le zeste de lorange.
            • Hachez loignon et les épinards.
        """.trimIndent()

        val offTaxonomyNames = listOf("orange", "oignon", "épinard")
        val result = RecipeOcrParser.parse(ocrText, vocabulary, offTaxonomyList = offTaxonomyNames)

        val instructions = result.instructions ?: ""

        assertWithMessage("Le mot 'lorange' doit être restauré en 'l'orange' grâce à la liste pre-chargée OFF de Room DB")
            .that(instructions)
            .contains("Prélevez finement le zeste de l'orange.")

        assertWithMessage("Le mot 'loignon' doit être restauré en 'l'oignon' grâce à la liste pre-chargée OFF de Room DB")
            .that(instructions)
            .contains("Hachez l'oignon et les épinards.")
    }
}
