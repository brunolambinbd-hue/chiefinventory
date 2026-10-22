package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CulinaryUnitsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `IngredientParser should correctly parse new unit abbreviation c a s`() {
        val parsed = IngredientParser.parse("2 c. à s. de mayonnaise", vocabulary.culinaryMeasurementUnits)

        assertThat(parsed.quantity).isEqualTo(2.0)
        assertThat(parsed.unit).isEqualTo("c. à s.")
        assertThat(parsed.name).isEqualTo("mayonnaise")
    }

    @Test
    fun `IngredientParser should correctly parse new unit abbreviation c a c`() {
        val parsed = IngredientParser.parse("1 c. à c. de miel", vocabulary.culinaryMeasurementUnits)

        assertThat(parsed.quantity).isEqualTo(1.0)
        assertThat(parsed.unit).isEqualTo("c. à c.")
        assertThat(parsed.name).isEqualTo("miel")
    }

    @Test
    fun `IngredientParser should correctly parse new unit abbreviation c a d and c a dessert`() {
        val parsed1 = IngredientParser.parse("1 c. à d. de rhum", vocabulary.culinaryMeasurementUnits)
        assertThat(parsed1.quantity).isEqualTo(1.0)
        assertThat(parsed1.unit).isEqualTo("c. à d.")
        assertThat(parsed1.name).isEqualTo("rhum")

        val parsed2 = IngredientParser.parse("1 c. à dessert de rhum", vocabulary.culinaryMeasurementUnits)
        assertThat(parsed2.quantity).isEqualTo(1.0)
        assertThat(parsed2.unit).isEqualTo("c. à dessert")
        assertThat(parsed2.name).isEqualTo("rhum")
    }

    @Test
    fun `IngredientParser should correctly parse plural units and abbreviations dynamically`() {
        val customUnits = vocabulary.culinaryMeasurementUnits

        val parsed1 = IngredientParser.parse("3 cuillères à soupe de farine", customUnits)
        assertThat(parsed1.quantity).isEqualTo(3.0)
        assertThat(parsed1.unit).isEqualTo("cuillères à soupe")
        assertThat(parsed1.name).isEqualTo("farine")

        val parsed2 = IngredientParser.parse("4 gousses d'ail", customUnits)
        assertThat(parsed2.quantity).isEqualTo(4.0)
        assertThat(parsed2.unit).isEqualTo("gousses")
        assertThat(parsed2.name).isEqualTo("ail")

        val parsed3 = IngredientParser.parse("2 bottes de persil", customUnits)
        assertThat(parsed3.quantity).isEqualTo(2.0)
        assertThat(parsed3.unit).isEqualTo("bottes")
        assertThat(parsed3.name).isEqualTo("persil")

        val parsed4 = IngredientParser.parse("3 sachets de levure", customUnits)
        assertThat(parsed4.quantity).isEqualTo(3.0)
        assertThat(parsed4.unit).isEqualTo("sachets")
        assertThat(parsed4.name).isEqualTo("levure")

        val parsed5 = IngredientParser.parse("2 verres d'eau", customUnits)
        assertThat(parsed5.quantity).isEqualTo(2.0)
        assertThat(parsed5.unit).isEqualTo("verres")
        assertThat(parsed5.name).isEqualTo("eau")

        val parsed6 = IngredientParser.parse("2 filets d'huile d'olive", customUnits)
        assertThat(parsed6.quantity).isEqualTo(2.0)
        assertThat(parsed6.unit).isEqualTo("filets")
        assertThat(parsed6.name).isEqualTo("huile d'olive")

        val parsed7 = IngredientParser.parse("3 brins d'aneth", customUnits)
        assertThat(parsed7.quantity).isEqualTo(3.0)
        assertThat(parsed7.unit).isEqualTo("brins")
        assertThat(parsed7.name).isEqualTo("aneth")
    }

    @Test
    fun `Ocr1Normalizer and IngredientParser should handle casde and cacde typos from ocr_repairs`() {
        val normalized1 = Ocr1Normalizer.normalize("2 casde mayonnaise", vocabulary.ocrSpellingRepairs)
        val parsed1 = IngredientParser.parse(normalized1, vocabulary.culinaryMeasurementUnits)
        assertThat(parsed1.quantity).isEqualTo(2.0)
        assertThat(parsed1.unit).isAnyOf("c. à s.", "c. à soupe")
        assertThat(parsed1.name).isEqualTo("mayonnaise")

        val normalized2 = Ocr1Normalizer.normalize("1 cacde miel", vocabulary.ocrSpellingRepairs)
        val parsed2 = IngredientParser.parse(normalized2, vocabulary.culinaryMeasurementUnits)
        assertThat(parsed2.quantity).isEqualTo(1.0)
        assertThat(parsed2.unit).isAnyOf("c. à c.", "c. à café")
        assertThat(parsed2.name).isEqualTo("miel")
    }

    @Test
    fun `vocabulary should load all culinary_measurement_units from XML`() {
        assertThat(vocabulary.culinaryMeasurementUnits).contains("c. à s.")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("c. à c.")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("c. à d.")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("c. à dessert")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("cuillères à soupe")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("gousses")
        assertThat(vocabulary.culinaryMeasurementUnits).contains("sachets")
    }
}
