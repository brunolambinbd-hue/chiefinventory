package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.Ocr3Categorizer
import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Le nom propre de chef 'Chef Nahit YILMAZ' doit être préservé dans la liste des sources")
            .that(result.detectedSourceList)
            .isNotEmpty()
    }

    @Test
    fun `categorize should not classify line with culinary ingredients and quantities as wine even if MO is present`() {
        val input = listOf("6 aubergines 150 g de beurre M.O.")
        val result = Ocr3Categorizer.categorize(input, baseVocabulary)

        assertWithMessage("Une ligne d'ingrédients quantifiés avec M.O. ne doit pas être classée comme vin")
            .that(result.detectedWineList)
            .isEmpty()

        assertWithMessage("La ligne doit être conservée dans les ingrédients bruts")
            .that(result.rawIngredientsList)
            .contains("6 aubergines 150 g de beurre M.O.")
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

        assertWithMessage("La section vin doit extraire les 3 lignes de recommandation de vin")
            .that(result.detectedWineList)
            .containsExactly(
                "Château de la Fossardière",
                "Muscadet de Sèvre-et-Maine sur Lie",
                "MDC, 1996, 75 cl"
            )
            .inOrder()
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

        assertWithMessage("La section vin doit être fermée après la ligne de volume '75 cl'")
            .that(result.detectedWineList)
            .containsExactly(
                "Château de la Fossardière",
                "MDC, 1996, 75 cl"
            )
            .inOrder()

        assertWithMessage("Le bruit d'étiquette de bouteille ne doit pas fuiter vers les instructions")
            .that(result.rawInstructionsList)
            .doesNotContain("CHATEAU ala FoSARDJERE")
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

        assertWithMessage("La consigne d'instruction doit être conservée")
            .that(result.rawInstructionsList)
            .containsExactly("Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.")

        assertWithMessage("Le bloc de vin doit être extrait dans detectedWineList")
            .that(result.detectedWineList)
            .containsExactly(
                "Château de la Fossardière",
                "MDC, 1996, 75 cl"
            )
            .inOrder()

        assertWithMessage("Le bruit de bas de page ne doit pas figurer dans les instructions")
            .that(result.rawInstructionsList)
            .doesNotContain("ala FoSARDJERE")
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

        assertWithMessage("L'ingrédient 'vinaigre de vin blanc' ne doit pas être classé comme vin")
            .that(result.detectedWineList)
            .isEmpty()

        assertWithMessage("L'ingrédient complet '4 c. à soupe de vinaigre de vin blanc' doit figurer dans rawIngredientsList")
            .that(result.rawIngredientsList)
            .contains("4 c. à soupe de vinaigre de vin blanc")
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

        assertWithMessage("La suite de la consigne d'instruction ne doit pas être volée par les ingrédients")
            .that(result.rawIngredientsList)
            .doesNotContain("les feuilles et le cœur.")

        assertWithMessage("La suite de la consigne doit rester dans rawInstructionsList")
            .that(result.rawInstructionsList)
            .contains("les feuilles et le cœur.")
    }

    @Test
    fun `categorize should support synthetic custom action keyword complement`() {
        val customVocabulary = baseVocabulary.copy(
            stepActionKeywords = baseVocabulary.stepActionKeywords + "flamber_test"
        )
        val input = listOf("flamber_test les crêpes")
        val result = Ocr3Categorizer.categorize(input, customVocabulary)

        assertWithMessage("Le mot-clé personnalisé 'flamber_test' doit orienter la ligne vers les instructions")
            .that(result.rawInstructionsList)
            .contains("flamber_test les crêpes")
    }
}
