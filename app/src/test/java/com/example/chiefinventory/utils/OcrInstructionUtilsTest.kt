package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrInstructionUtils].
 */
class OcrInstructionUtilsTest {

    @Test
    fun `hasInstructionTrigger should detect trigger keywords`() {
        val triggers = listOf("préparation", "étape", "réalisation")
        assertThat(OcrInstructionUtils.hasInstructionTrigger("Préparation :", triggers)).isTrue()
        assertThat(OcrInstructionUtils.hasInstructionTrigger("Ingrédients", triggers)).isFalse()
    }

    @Test
    fun `hasActionVerb should detect imperative action verbs`() {
        val keywords = listOf("Coupez", "Mélangez", "Servez")
        assertThat(OcrInstructionUtils.hasActionVerb("Coupez les légumes en dés", keywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("200 g de farine", keywords)).isFalse()
    }

    @Test
    fun `hasActionVerb should detect newly integrated action verbs like maintenez poursuivez obteniez`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        
        assertThat(OcrInstructionUtils.hasActionVerb("Maintenez une petite ébullition", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("Poursuivez la cuisson 15 min", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("jusqu'à ce que vous obteniez une belle salade", vocabulary.stepActionKeywords)).isTrue()
        assertThat(OcrInstructionUtils.hasActionVerb("Conservez au frais", vocabulary.stepActionKeywords)).isTrue()
    }
}
