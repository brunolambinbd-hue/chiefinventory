package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrInstructionUtils].
 */
class OcrInstructionUtilsTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasInstructionTrigger_DetectsTriggerKeywordsAndHandlesBlankEmpty() {
        val triggers = listOf("préparation", "étape", "réalisation")
        assertThat(OcrInstructionUtils.hasInstructionTrigger("Préparation :", triggers)).isTrue()
        assertThat(OcrInstructionUtils.hasInstructionTrigger("Ingrédients", triggers)).isFalse()

        // Blank line & empty triggers
        assertThat(OcrInstructionUtils.hasInstructionTrigger("", triggers)).isFalse()
        assertThat(OcrInstructionUtils.hasInstructionTrigger("   ", triggers)).isFalse()
        assertThat(OcrInstructionUtils.hasInstructionTrigger("Préparation", emptyList())).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasActionVerb_DetectsImperativeActionVerbsAndHandlesBlankEmptyFilter() {
        val keywords = listOf("Coupez", "Mélangez", "Servez")
        assertThat(OcrInstructionUtils.hasActionVerb("Coupez les légumes en dés", keywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("200 g de farine", keywords)).isFalse()

        // Blank line & empty keywords
        assertThat(OcrInstructionUtils.hasActionVerb("", keywords)).isFalse()
        assertThat(OcrInstructionUtils.hasActionVerb("Coupez", emptyList())).isFalse()

        // Keywords filtered out (short < 3 chars or in excluded words list)
        assertThat(OcrInstructionUtils.hasActionVerb("couper", listOf("a", "minutes", "heures"))).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasActionVerb_DetectsNewlyIntegratedActionVerbs() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        assertThat(OcrInstructionUtils.hasActionVerb("Maintenez une petite ébullition", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("Poursuivez la cuisson 15 min", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("jusqu'à ce que vous obteniez une belle salade", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("Conservez au frais", vocabulary.stepActionKeywords)).isTrue()
    }
}
