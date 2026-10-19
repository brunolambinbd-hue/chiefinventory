package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Ocr5BeautifierTest {

    @Test
    fun beautifyInstructions_removesStepNumbersAndPunctuation() {
        val input = listOf("1. Couper les oignons", "2) Ajouter le sel", "3- Mélanger", "10 minutes au four")
        val expected = listOf("Couper les oignons.", "Ajouter le sel.", "Mélanger.", "10 minutes au four.")
        
        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun beautifyInstructions_removesIsolatedDigitsBeforeUppercaseWords() {
        val input = listOf("1 Plongez les légumes")
        val expected = listOf("Plongez les légumes.")
        
        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertThat(result).containsExactlyElementsIn(expected)
    }

    @Test
    fun beautifyInstructions_removesBullets() {
        val input = listOf("• Faire cuire", "- Saler", "* Poivrer")
        val expected = listOf("Faire cuire.", "Saler.", "Poivrer.")
        
        val result = Ocr5Beautifier.beautifyInstructions(input)
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun beautifyIngredients_capitalizesFirstLetter() {
        val input = listOf("sel", "poivre", "2 carottes")
        val expected = listOf("Sel", "Poivre", "2 carottes")
        
        val result = Ocr5Beautifier.beautifyIngredients(input)
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }
}
