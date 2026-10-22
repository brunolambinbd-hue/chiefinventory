package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la détection des en-têtes de section
 * et le basculement d'état par [Ocr3CategorizerHeader].
 */
class Ocr3CategorizerHeaderTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie la détection d'en-tête d'ingrédients complexe ("ingrédients pour 4 personnes").
     */
    @Test
    fun `processSectionHeader should detect ingredients header and update state`() {
        val state = Ocr3CategorizerHeader.HeaderState()
        val seenKeywords = mutableSetOf("farine", "sucre")

        val isHeader = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "ingrédients pour 4 personnes",
            lowerLine = "ingrédients pour 4 personnes",
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = seenKeywords
        )

        assertThat(isHeader).isTrue()
        assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INGREDIENTS)
        assertThat(state.isIngredientListClosed).isFalse()
        assertThat(seenKeywords).isEmpty()
    }

    /**
     * Vérifie la détection d'en-tête d'instructions ("préparation:").
     */
    @Test
    fun `processSectionHeader should detect preparation header and switch to instructions section`() {
        val state = Ocr3CategorizerHeader.HeaderState()
        val seenKeywords = mutableSetOf<String>()

        val isHeader = Ocr3CategorizerHeader.processSectionHeader(
            workingLine = "Préparation :",
            lowerLine = "préparation :",
            containsAction = false,
            vocabulary = vocabulary,
            state = state,
            seenIngredientKeywords = seenKeywords
        )

        assertThat(isHeader).isTrue()
        assertThat(state.currentSection).isEqualTo(Ocr3Categorizer.SECTION_INSTRUCTIONS)
    }
}
