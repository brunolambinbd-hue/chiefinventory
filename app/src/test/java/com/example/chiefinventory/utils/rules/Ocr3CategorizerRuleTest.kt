package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.Ocr3Categorizer
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class Ocr3CategorizerRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `categorize should preserve source proper names before applying excluded keywords filter`() {
        val input = listOf("Chef Nahit YILMAZ")
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.detectedSourceList).isNotEmpty()
    }

    @Test
    fun `categorize should not classify line with culinary ingredients and quantities as wine even if MO is present`() {
        val input = listOf("6 aubergines 150 g de beurre M.O.")
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.detectedWineList).isEmpty()
        assertThat(result.rawIngredientsList).contains("6 aubergines 150 g de beurre M.O.")
    }

    @Test
    fun `categorize should close wine section after volume line and ignore bottle photo label noise`() {
        val input = listOf(
            "VIN CONSEILLÉ",
            "Château de la Fossardière",
            "Muscadet de Sèvre-et-Maine sur Lie",
            "MDC, 1996, 75 cl",
            "CHATEAU de la FOSSARDIERE"
        )
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.detectedWineList).containsExactly(
            "Château de la Fossardière",
            "Muscadet de Sèvre-et-Maine sur Lie",
            "MDC, 1996, 75 cl"
        ).inOrder()
    }

    @Test
    fun `categorize should discard bottle photo label wine text after wine section closed and not leak into instructions`() {
        val input = listOf(
            "VIN CONSEILLÉ",
            "Château de la Fossardière",
            "MDC, 1996, 75 cl",
            "CHATEAU ala FoSARDJERE"
        )
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.detectedWineList).containsExactly(
            "Château de la Fossardière",
            "MDC, 1996, 75 cl"
        ).inOrder()
        assertThat(result.rawInstructionsList).doesNotContain("CHATEAU ala FoSARDJERE")
    }

    @Test
    fun `categorize should close instructions section when wine block is processed and discard trailing footer noise`() {
        val input = listOf(
            "• Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.",
            "VIN CONSEILLÉ",
            "Château de la Fossardière",
            "MDC, 1996, 75 cl",
            "ala FoSARDJERE"
        )
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.rawInstructionsList).containsExactly("Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.")
        assertThat(result.detectedWineList).containsExactly(
            "Château de la Fossardière",
            "MDC, 1996, 75 cl"
        ).inOrder()
        assertThat(result.rawInstructionsList).doesNotContain("ala FoSARDJERE")
    }

    @Test
    fun `categorize should not steal wine from multi line ingredient inside active ingredient section`() {
        val input = listOf(
            "INGRÉDIENTS",
            "Pour la vinaigrette :",
            "4 c. à soupe de vinaigre de",
            "vin blanc"
        )
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.detectedWineList).isEmpty()
        assertThat(result.rawIngredientsList).contains("4 c. à soupe de vinaigre de vin blanc")
    }

    @Test
    fun `categorize should not pull back instruction continuation lines without hard quantity`() {
        val input = listOf(
            "PRÉPARATION",
            "Servez avec la vinaigrette",
            "dans laquelle vous tremperez",
            "les feuilles et le cœur."
        )
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertThat(result.rawIngredientsList).doesNotContain("les feuilles et le cœur.")
        assertThat(result.rawInstructionsList).contains("les feuilles et le cœur.")
    }

    @Test
    fun `categorize should support synthetic custom action keyword complement`() {
        val customVocabulary = baseVocabulary.copy(
            stepActionKeywords = baseVocabulary.stepActionKeywords + "flamber_test"
        )
        val input = listOf("flamber_test les crêpes")
        val result = Ocr3Categorizer.categorize(input, customVocabulary)

        assertThat(result.rawInstructionsList).contains("flamber_test les crêpes")
    }
}
