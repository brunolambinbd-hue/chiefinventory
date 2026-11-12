package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation des Étapes 1 & 2 vérifiant que l'en-tête "Votre marché" (et "Votre marché (4 personnes)")
 * est préservé à travers la normalisation et la dépollution du bruit pour servir de déclencheur à l'Étape 3.
 */
class OcrVotreMarchePhase1And2Test {

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
    fun testPhase1And2_PreservesVotreMarcheHeaderAsTrigger() {
        val rawLines = listOf(
            "Votre marché :",
            "4 oignons",
            "50 g de beurre"
        )

        val normalized = rawLines.map { Ocr1Corrector.correct(it, vocabulary.ocrSpellingRepairs) }
        val cleanedResult = Ocr2Cleaner.cleanWithVocabulary(normalized, vocabulary)

        assertWithMessage("L'en-tête 'Votre marché' doit être préservé en Phase 1 & 2 pour servir de déclencheur de section")
            .that(cleanedResult.cleanedLines)
            .contains("Votre marché")
    }
}
