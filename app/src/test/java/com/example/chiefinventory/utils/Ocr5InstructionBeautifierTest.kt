package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la beautification, la ponctuation et le recollage de consignes
 * suspendues par [Ocr5InstructionBeautifier].
 */
class Ocr5InstructionBeautifierTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie le recollage de phrases d'instructions rompues après une conjonction "et." ("et. Garnissez" -> "et garnissez").
     */
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

    /**
     * Vérifie le recollage de consignes coupées après une abréviation d'unité ("aux 4 c." + "À soupe" -> "aux 4 c. à soupe").
     */
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

    /**
     * Vérifie le recollage de phrases rompues par une virgule finale (",. Le thym" -> ", le thym").
     */
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

    /**
     * Vérifie que les consignes déjà complètes se terminant par un point restent séparées.
     */
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
