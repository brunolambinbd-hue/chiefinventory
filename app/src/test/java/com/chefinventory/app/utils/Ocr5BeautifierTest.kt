package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant les fonctionnalités de présentation finale de [Ocr5Beautifier].
 */
class Ocr5BeautifierTest {

    /**
     * Vérifie la suppression des numéros d'étapes et puces de numérotation.
     */
    @Test
    fun beautifyInstructions_removesStepNumbersAndPunctuation() {
        val input = listOf("1. Couper les oignons", "2) Ajouter le sel", "3- Mélanger", "10 minutes au four")
        val expected = listOf("Couper les oignons.", "Ajouter le sel.", "Mélanger.", "10 minutes au four.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Les numéros d'étapes ('1.', '2)', '3-') doivent être supprimés et les consignes majusculées et ponctuées")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    /**
     * Vérifie la suppression des chiffres isolés en début de consigne devant les majuscules.
     */
    @Test
    fun beautifyInstructions_removesIsolatedDigitsBeforeUppercaseWords() {
        val input = listOf("1 Plongez les légumes")
        val expected = listOf("Plongez les légumes.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Le chiffre isolé '1' devant le mot majusculé 'Plongez' doit être retiré")
            .that(result)
            .containsExactlyElementsIn(expected)
    }

    /**
     * Vérifie le retrait des puces typographiques en début de ligne (`•`, `-`, `*`).
     */
    @Test
    fun beautifyInstructions_removesBullets() {
        val input = listOf("• Faire cuire", "- Saler", "* Poivrer")
        val expected = listOf("Faire cuire.", "Saler.", "Poivrer.")

        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertWithMessage("Les puces '•', '-', '*' doivent être retirées en début de ligne")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    /**
     * Vérifie la majuscule initiale forcée sur chaque nom d'ingrédient.
     */
    @Test
    fun beautifyIngredients_capitalizesFirstLetter() {
        val input = listOf("sel", "poivre", "2 carottes")
        val expected = listOf("Sel", "Poivre", "2 carottes")

        val result = Ocr5Beautifier.beautifyIngredients(input)
        assertWithMessage("Chaque ingrédient en minuscules doit recevoir une majuscule initiale")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }
}
