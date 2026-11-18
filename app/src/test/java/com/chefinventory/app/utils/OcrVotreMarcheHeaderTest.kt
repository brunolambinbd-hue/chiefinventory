package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la détection de l'en-tête de section 'Votre marché:'
 * et le découpage propre des ingrédients associés (Asperges aux crevettes grises).
 */
class OcrVotreMarcheHeaderTest {

    private val rawOcrScanText = listOf(
        "ASPERGES AUX CREVETTES GRISES",
        "40 MIN 4 PERSONNES FACILE RAISONNABLE",
        "Votre marché: • 2 bottes d'asperges 200 g de crevettes grises",
        "INSTRUCTIONS",
        "• Cuisez les asperges à l'eau bouillante salée."
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
    fun testVotreMarcheHeader_ExtractsBothIngredientsCleanly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        val ingredients = result.ingredients?.split("\n") ?: emptyList()

        assertWithMessage("Le premier ingrédient '2 bottes d'asperges' doit être extrait")
            .that(ingredients.any { it.contains("asperges", ignoreCase = true) })
            .isTrue()

        assertWithMessage("Le deuxième ingrédient '200 g de crevettes grises' doit être extrait")
            .that(ingredients.any { it.contains("crevettes", ignoreCase = true) })
            .isTrue()
    }
}
