package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Les ingrédients doivent être majusculés en premier caractère et les lignes vides purgées")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_FiltersOutResidualHeadersAndTrashWords() {
        val input = listOf("INGRÉDIENTS:", "ingredients:", "composition:", "ngrédients:", "les", "du", "200 g de farine")
        val expected = listOf("200 g de farine")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input)
        assertWithMessage("Les en-têtes d'ingrédients résiduels et articles isolés doivent être éliminés")
            .that(result)
            .containsExactlyElementsIn(expected)
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

        assertWithMessage("Le qualificatif de variété orphelin 'princesses' doit être regroupé avec '200 g de haricots'")
            .that(result)
            .containsExactly("200 g de haricots princesses")
    }
}
