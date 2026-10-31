package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
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

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_ExhaustiveBranchVariations() {
        val input = listOf(
            "Ə RECETTES DU CHEF",
            "consignes",
            "preparation",
            "Mélanger les ingrédients ou.",
            "Ajoutez du sel,.",
            "Poivrez.",
            "Servir chaud !",
            "Déguster chaud ?"
        )

        val result = Ocr5InstructionBeautifier.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits,
            vocabulary.ocrSpellingRepairs
        )

        assertThat(result).contains("Mélanger les ingrédients ou ajoutez du sel, poivrez.")
        assertThat(result).contains("Servir chaud !")
        assertThat(result).contains("Déguster chaud ?")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_DefaultArgumentsAndBlankLinesAndTrailingPeriod() {
        val input = listOf(
            "",
            "   ",
            "PRÉPARATION",
            "INSTRUCTIONS",
            "CONSIGNES",
            "FICHE RECETTE",
            "mélangez l'huile d'olive",
            "   ",
            "laissez mariner au réfrigérateur"
        )

        // Test with default arguments
        val result = Ocr5InstructionBeautifier.beautifyInstructions(input)

        assertThat(result).contains("Mélangez l'huile d'olive.")
        assertThat(result).contains("Laissez mariner au réfrigérateur.")
        assertThat(result).doesNotContain("PRÉPARATION")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterConjunctionEtDot() {
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
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterUnitAbbreviationCDot() {
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
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterComma() {
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
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_LeaveCompleteSentencesEndingWithAPeriodSeparate() {
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
