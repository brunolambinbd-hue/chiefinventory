package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant le découpage de phrases par [Ocr4MergerSentenceSplitter]
 * sans jamais couper après une abréviation culinaire se terminant par un point ("th.", "c.", "temp.").
 */
class Ocr4MergerSentenceSplitterTest {

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
    fun testSplitIntoSentences_DoesNotSplitOnThermostatAbbreviationDot() {
        val block = "Préchauffer le four sur th. 7. Cuire 20 min."

        val sentences = Ocr4MergerSentenceSplitter.splitIntoSentences(block, vocabulary)

        assertWithMessage("Le découpage de phrases ne doit pas couper après 'th.' et doit produire 2 phrases")
            .that(sentences)
            .containsExactly(
                "Préchauffer le four sur th. 7.",
                "Cuire 20 min."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitIntoSentences_DoesNotSplitOnSpoonAbbreviationDot() {
        val block = "Ajouter 2 c. à soupe de farine. Mélanger."

        val sentences = Ocr4MergerSentenceSplitter.splitIntoSentences(block, vocabulary)

        assertWithMessage("Le découpage de phrases ne doit pas couper après 'c.' et doit produire 2 phrases")
            .that(sentences)
            .containsExactly(
                "Ajouter 2 c. à soupe de farine.",
                "Mélanger."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitIntoSentences_DoesNotSplitOnTemperatureAbbreviationDot() {
        val block = "Cuire à temp. 180°C. Servir chaud."

        val sentences = Ocr4MergerSentenceSplitter.splitIntoSentences(block, vocabulary)

        assertWithMessage("Le découpage de phrases ne doit pas couper après 'temp.' et doit produire 2 phrases")
            .that(sentences)
            .containsExactly(
                "Cuire à temp. 180°C.",
                "Servir chaud."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitIntoSentences_PreservesFullThermostatNemsSentence() {
        val block = "Vous pouvez faire dorer les nems dans la friteuse (180) ou alors au four (préchauffé sur th. 7 - 210). Laissez reposer."

        val sentences = Ocr4MergerSentenceSplitter.splitIntoSentences(block, vocabulary)

        assertWithMessage("La consigne des nems au thermostat 'th. 7 - 210' ne doit pas être coupée après 'th.'")
            .that(sentences)
            .containsExactly(
                "Vous pouvez faire dorer les nems dans la friteuse (180) ou alors au four (préchauffé sur th. 7 - 210).",
                "Laissez reposer."
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSplitIntoSentences_HandlesEmptyAndBlankText() {
        assertWithMessage("Une chaîne vide doit retourner une liste vide")
            .that(Ocr4MergerSentenceSplitter.splitIntoSentences("", vocabulary))
            .isEmpty()

        assertWithMessage("Une chaîne d'espaces doit retourner une liste vide")
            .that(Ocr4MergerSentenceSplitter.splitIntoSentences("   ", vocabulary))
            .isEmpty()
    }
}
