package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et les règles de beautification d'ingrédients de [Ocr5BeautifierIngredient].
 */
class Ocr5BeautifierIngredientTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_CapitalizesFirstLetterAndHandlesUppercaseDigitsAndEmpty() {
        val input = listOf("sel", "poivre du moulin", "2 carottes", "Farine", "", "   ")
        val expected = listOf("Sel", "Poivre du moulin", "2 carottes", "Farine")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input)
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_FiltersOutResidualHeadersAndTrashWords() {
        val input = listOf("INGRÉDIENTS:", "ingredients:", "composition:", "ngrédients:", "les", "du", "200 g de farine")
        val expected = listOf("200 g de farine")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input)
        assertThat(result).containsExactlyElementsIn(expected)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_MergesOrphanVarietyQualifiers() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val input = listOf("200 g de haricots", "princesses")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(
            input,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )

        assertThat(result).containsExactly("200 g de haricots princesses")
    }
}
