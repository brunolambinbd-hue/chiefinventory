package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction d'ingrédients implicites par [Ocr3NarrativeIngredientExtractor].
 */
class Ocr3NarrativeIngredientExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsDurationOrTime_DetectsDurationsAndTemperaturesAndRejectsFood() {
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("15 min", "min", "minutes")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("20 mn", "mn", "mn")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("30 sec", "sec", "secondes")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("2 h", "h", "heures")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("180°C", null, "180°C")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("200 °", null, "200 °")).isTrue()
        assertThat(Ocr3NarrativeIngredientExtractor.isDurationOrTime("200 g de farine", "g", "farine")).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_QuantitiesWithAndWithoutUnitsAndDecimalQuantities() {
        val results = RawSections()
        val instructions = listOf(
            "Ajoutez 0.5 l de lait et 3 oeufs.",
            "Incorporez 250 g de farine et 1 sachet de levure.",
            "Enfournez à 180°C pendant 20 minutes.",
            "Assaisonnez avec du sel."
        )

        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructions, vocabulary, results)

        assertThat(results.rawIngredientsList).contains("0.5 l lait")
        assertThat(results.rawIngredientsList).contains("3 oeufs")
        assertThat(results.rawIngredientsList).contains("250 g farine")
        assertThat(results.rawIngredientsList).contains("Sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_TechnicalDimensionsAndShortWordsAndDuplicates() {
        val results = RawSections()
        results.rawIngredientsList.add("Sel")

        val instructions = listOf(
            "Couper le concombre en tronçons de 5 mm sur 5 mm.",
            "Ajoutez 2 g d'ail pour vous et nous sur feu moyen.",
            "Ajoutez 2 cuillères à soupe d'huile.",
            "Incorporez 200 g de farine puis encore 100 g de farine.",
            "Ajoutez 2 g de de a b.",
            "Agrémentez de poivre."
        )

        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructions, vocabulary, results)

        assertThat(results.rawIngredientsList).contains("2 g ail et sur")
        assertThat(results.rawIngredientsList).contains("Poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_EmptyInstructionLines_ReturnsEmpty() {
        val results = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(emptyList(), vocabulary, results)

        assertThat(results.rawIngredientsList).isEmpty()
    }
}
