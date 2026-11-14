package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la génération dynamique et le tri strict Longest Match First
 * des assaisonnements de poivres et sels de variétés d'origines dans [OcrVocabularyLoader.commonIngredientsNoQty].
 */
class OcrDynamicSeasoningVocabularyTest {

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
    fun testCommonIngredientsNoQty_ContainsDynamicallyGeneratedPepperAndSaltPhrases() {
        val common = vocabulary.commonIngredientsNoQty

        assertWithMessage("Le vocabulaire doit contenir dynamiquement 'poivre de Sichuan'")
            .that(common)
            .contains("poivre de Sichuan")

        assertWithMessage("Le vocabulaire doit contenir dynamiquement 'poivre du moulin'")
            .that(common)
            .contains("poivre du moulin")

        assertWithMessage("Le vocabulaire doit contenir dynamiquement 'poivre de Kampot'")
            .that(common)
            .contains("poivre de Kampot")

        assertWithMessage("Le vocabulaire doit contenir dynamiquement 'sel de Guérande'")
            .that(common)
            .contains("sel de Guérande")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCommonIngredientsNoQty_IsStrictlySortedByLengthDescending() {
        val common = vocabulary.commonIngredientsNoQty

        val isSortedByLengthDescending = common.zipWithNext().all { (a, b) -> a.length >= b.length }

        assertWithMessage("Le dictionnaire commonIngredientsNoQty doit être trié par longueur décroissante (Longest Match First)")
            .that(isSortedByLengthDescending)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_RecipeWithStandalonePoivreDuMoulin_ExtractsIngredientCleanly() {
        val rawOcrText = """
            Steak au poivre
            INGRÉDIENTS
            2 steaks
            poivre du moulin
            INSTRUCTIONS
            • Faites cuire les steaks.
        """.trimIndent()

        val parsed = RecipeOcrParser.parse(rawOcrText, vocabulary)

        assertWithMessage("La recette doit extraire l'ingrédient 'Poivre du moulin'")
            .that(parsed.ingredients.orEmpty())
            .contains("Poivre du moulin")
    }
}
