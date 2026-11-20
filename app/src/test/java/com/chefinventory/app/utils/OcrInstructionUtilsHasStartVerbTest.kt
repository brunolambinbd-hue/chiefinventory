package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [OcrInstructionUtils.hasStartVerb]
 * vérifiant la détection exacte des verbes de démarrage impératifs et la gestion des puces caméra.
 */
class OcrInstructionUtilsHasStartVerbTest {

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
    fun testHasStartVerb_ExhaustiveStartVerbDetections() {
        val startVerbs = vocabulary.stepStartVerbs

        // 1. Verbes avec puces caméra ('O ', '0 ', '• ') et majuscule accentuée
        assertWithMessage("La ligne 'O Épluchez les pommes de terre' doit retourner true pour 'hasStartVerb'")
            .that(OcrInstructionUtils.hasStartVerb("O Épluchez les pommes de terre et taillez-", startVerbs))
            .isTrue()

        assertWithMessage("La ligne 'Épluchez les pommes de terre' sans puce doit retourner true")
            .that(OcrInstructionUtils.hasStartVerb("Épluchez les pommes de terre", startVerbs))
            .isTrue()

        assertWithMessage("La ligne 'O Faites revenir l'oignon' doit retourner true")
            .that(OcrInstructionUtils.hasStartVerb("O Faites revenir l'oignon haché à feu doux", startVerbs))
            .isTrue()

        assertWithMessage("La ligne 'Faites revenir l'oignon' sans puce doit retourner true")
            .that(OcrInstructionUtils.hasStartVerb("Faites revenir l'oignon haché à feu doux", startVerbs))
            .isTrue()

        // 2. Faux positifs (Lignes d'ingrédients)
        assertWithMessage("La ligne d'ingrédient '1 poireau' ne doit pas être qualifiée de verbe de démarrage")
            .that(OcrInstructionUtils.hasStartVerb("1 poireau", startVerbs))
            .isFalse()

        assertWithMessage("La ligne d'ingrédient '• 200 g d'épinards' ne doit pas être qualifiée de verbe de démarrage")
            .that(OcrInstructionUtils.hasStartVerb("• 200 g d'épinards", startVerbs))
            .isFalse()

        assertWithMessage("La ligne d'ingrédient '• Sel' ne doit pas être qualifiée de verbe de démarrage")
            .that(OcrInstructionUtils.hasStartVerb("• Sel", startVerbs))
            .isFalse()
    }
}
