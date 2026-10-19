package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrArtichautRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse full artichaut recipe lines should produce clean structured result`() {
        val rawLines = listOf(
            "ingrédients",
            "POUR",
            "4 PERSONNES",
            "4 artichauts",
            "2 l. d'eau",
            "le jus de 1 citron",
            "4 c. à soupe de farine",
            "sel et poivre",
            "Pour la vinaigrette",
            "2 c. à soupe de moutarde",
            "forte",
            "2 échalotes",
            "4 c. à soupe de vinaigre de",
            "vin blanc",
            "2 c. à soupe d'eau",
            "16 c. à soupe d'huile",
            "d'olive douce",
            "1 c. à soupe de persil",
            "haché",
            "• Mélangez la farine avec un",
            "peu d'eau et le jus de citron.",
            "Mettez ce mélange avec le",
            "reste d'eau dans une grande",
            "casserole et ajoutez-y les",
            "4 artichauts. Salez et poivrez.",
            "o/rtichauts et vinaigrette",
            "aux échalotes",
            "• Faites cuire pendant 1 heure",
            "à feu doux.",
            "Assiette de présentation disponible dans les Maxi GB",
            "• Pendant ce temps, préparez",
            "la vinaigrette et ajoutez-y au",
            "dernier moment les échalotes",
            "émincées et le persil.",
            "• Egoutez les artichauts.",
            "Servez avec la vinaigrette",
            "dans laquelle vous tremperez",
            "les feuilles et le cœur.",
            "VIN CONSEILLE",
            "Château de la Fossardière",
            "Muscadet de Sèvre-et-Maine sur Lie",
            "MDC, I996, 75 cl",
            "CHATEAU",
            "ala FoSARDJERE"
        )

        val result = RecipeOcrParser.parse(rawLines.joinToString("\n"), vocabulary)

        // 1. Portions
        assertThat(result.servings).isEqualTo("4")

        // 2. Ingrédients
        val ing = result.ingredients ?: ""
        assertThat(ing).contains("4 c. à soupe de vinaigre de vin blanc")
        assertThat(ing).contains("16 c. à soupe d'huile d'olive douce")
        assertThat(ing).contains("1 c. à soupe de persil")
        assertThat(ing).contains("2 c. à soupe de moutarde forte")
        assertThat(ing).doesNotContain("Assiette de présentation")
        assertThat(ing).doesNotContain("FoSARDJERE")
        assertThat(ing).doesNotContain("les feuilles et le cœur")

        // 3. Instructions
        val inst = result.instructions ?: ""
        assertThat(inst).contains("Mélangez la farine avec un peu d'eau et le jus de citron.")
        assertThat(inst).contains("Faites cuire pendant 1 heure")
        assertThat(inst).contains("Pendant ce temps, préparez la vinaigrette et ajoutez-y au dernier moment les échalotes émincées et le persil.")
        assertThat(inst).contains("Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.")
        assertThat(inst).doesNotContain("Assiette de présentation")
        assertThat(inst).doesNotContain("FoSARDJERE")
        assertThat(inst).doesNotContain("CHATEAU")

        // 4. Vin
        val wine = result.wine ?: ""
        assertThat(wine).contains("Château de la Fossardière")
        assertThat(wine).contains("Muscadet de Sèvre-et-Maine sur Lie")
        assertThat(wine).doesNotContain("FoSARDJERE")
    }
}
