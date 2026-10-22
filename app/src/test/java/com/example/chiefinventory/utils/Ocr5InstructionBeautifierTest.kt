package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Ocr5InstructionBeautifierTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `beautifyInstructions should merge instruction sentence broken after conjunction et dot`() {
        val input = listOf(
            "Nappez de la sauce au yaourt et à la mayonnaise et.",
            "Garnissez de quelques branches de cerfeuil."
        )

        val result = Ocr5InstructionBeautifier.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertThat(result).containsExactly(
            "Nappez de la sauce au yaourt et à la mayonnaise et garnissez de quelques branches de cerfeuil."
        )
    }

    @Test
    fun `beautifyInstructions should merge instruction sentence broken after unit abbreviation c dot`() {
        val input = listOf(
            "Mélangez les légumes, la feta et l'estragon haché aux 4 c.",
            "À soupe de yaourt."
        )

        val result = Ocr5InstructionBeautifier.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits
        )

        assertThat(result).containsExactly(
            "Mélangez les légumes, la feta et l'estragon haché aux 4 c. à soupe de yaourt."
        )
    }

    @Test
    fun `beautifyInstructions should merge instruction sentence broken after comma`() {
        val input = listOf(
            "Mélangez l'huile d'olive, le piment,",
            "Le thym et le romarin effeuillé."
        )

        val result = Ocr5InstructionBeautifier.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertThat(result).containsExactly(
            "Mélangez l'huile d'olive, le piment, le thym et le romarin effeuillé."
        )
    }

    @Test
    fun `beautifyInstructions should leave complete sentences ending with a period separate`() {
        val input = listOf(
            "Mélangez l'huile d'olive et le thym.",
            "Laissez mariner au réfrigérateur."
        )

        val result = Ocr5InstructionBeautifier.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertThat(result).containsExactly(
            "Mélangez l'huile d'olive et le thym.",
            "Laissez mariner au réfrigérateur."
        )
    }
}
