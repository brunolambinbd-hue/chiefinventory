package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la détection des verbes de démarrage impératifs à majuscule accentuée
 * ('O Épluchez', 'O Égouttez', 'O Équeutez') précédés d'artefacts de puces caméra ('O ', '0 ', 'o ').
 */
class OcrAccentedStartVerbTest {

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
    fun testHasStartVerb_WithCameraBulletOAndCapitalAccentedVerb_ReturnsTrue() {
        val startVerbs = vocabulary.stepStartVerbs

        assertWithMessage("'O Épluchez les pommes de terre' doit être reconnu comme verbe de démarrage 'Épluchez'")
            .that(OcrInstructionUtils.hasStartVerb("O Épluchez les pommes de terre", startVerbs))
            .isTrue()

        assertWithMessage("'O Égouttez les légumes' doit être reconnu comme verbe de démarrage 'Égouttez'")
            .that(OcrInstructionUtils.hasStartVerb("O Égouttez les légumes", startVerbs))
            .isTrue()

        assertWithMessage("'O Équeutez les fraises' doit être reconnu comme verbe de démarrage 'Équeutez'")
            .that(OcrInstructionUtils.hasStartVerb("O Équeutez les fraises", startVerbs))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStripSingleLeadingAlphaOrBulletPrefix_WithCapitalAccentedVerb_StripsPrefixCleanly() {
        val cleaned1 = OcrInstructionUtils.stripSingleLeadingAlphaOrBulletPrefix("O Épluchez les pommes de terre")
        assertThat(cleaned1).isEqualTo("Épluchez les pommes de terre")

        val cleaned2 = OcrInstructionUtils.stripSingleLeadingAlphaOrBulletPrefix("O Égouttez les pâtes")
        assertThat(cleaned2).isEqualTo("Égouttez les pâtes")
    }
}
