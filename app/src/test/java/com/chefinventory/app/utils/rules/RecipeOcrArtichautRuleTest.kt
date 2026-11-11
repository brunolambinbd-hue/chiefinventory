package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Le nombre de personnes extrait doit valoir '4'")
            .that(result.servings)
            .isEqualTo("4")

        // 2. Ingrédients
        val ing = result.ingredients ?: ""
        assertWithMessage("Les ingrédients doivent contenir '4 c. à soupe de vinaigre de vin blanc'")
            .that(ing)
            .contains("4 c. à soupe de vinaigre de vin blanc")

        assertWithMessage("Les ingrédients doivent contenir '16 c. à soupe d'huile d'olive douce'")
            .that(ing)
            .contains("16 c. à soupe d'huile d'olive douce")

        assertWithMessage("Les ingrédients doivent contenir '1 c. à soupe de persil'")
            .that(ing)
            .contains("1 c. à soupe de persil")

        assertWithMessage("Les ingrédients doivent contenir '2 c. à soupe de moutarde forte'")
            .that(ing)
            .contains("2 c. à soupe de moutarde forte")

        assertWithMessage("Les bannières publicitaires ne doivent pas fuiter dans les ingrédients")
            .that(ing)
            .doesNotContain("Assiette de présentation")

        assertWithMessage("Le bruit d'étiquette 'FoSARDJERE' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .doesNotContain("FoSARDJERE")

        assertWithMessage("La suite de consigne 'les feuilles et le cœur' ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .doesNotContain("les feuilles et le cœur")

        // 3. Instructions
        val inst = result.instructions ?: ""
        assertWithMessage("Les instructions doivent contenir 'Mélangez la farine avec un peu d'eau'")
            .that(inst)
            .contains("Mélangez la farine avec un peu d'eau et le jus de citron.")

        assertWithMessage("Les instructions doivent contenir 'Faites cuire pendant 1 heure'")
            .that(inst)
            .contains("Faites cuire pendant 1 heure")

        assertWithMessage("Les instructions doivent contenir la suite de la vinaigrette")
            .that(inst)
            .contains("Pendant ce temps, préparez la vinaigrette et ajoutez-y au dernier moment les échalotes émincées et le persil.")

        assertWithMessage("Les instructions doivent contenir 'Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.'")
            .that(inst)
            .contains("Servez avec la vinaigrette dans laquelle vous tremperez les feuilles et le cœur.")

        assertWithMessage("Les bannières publicitaires ne doivent pas fuiter dans les instructions")
            .that(inst)
            .doesNotContain("Assiette de présentation")

        assertWithMessage("Le bruit 'FoSARDJERE' ne doit pas fuiter dans les instructions")
            .that(inst)
            .doesNotContain("FoSARDJERE")

        assertWithMessage("Le mot isolé 'CHATEAU' ne doit pas fuiter dans les instructions")
            .that(inst)
            .doesNotContain("CHATEAU")

        // 4. Vin
        val wine = result.wine ?: ""
        assertWithMessage("Le vin doit contenir 'Château de la Fossardière'")
            .that(wine)
            .contains("Château de la Fossardière")

        assertWithMessage("Le vin doit contenir 'Muscadet de Sèvre-et-Maine sur Lie'")
            .that(wine)
            .contains("Muscadet de Sèvre-et-Maine sur Lie")

        assertWithMessage("Le bruit d'étiquette 'FoSARDJERE' ne doit pas fuiter dans la recommandation de vin")
            .that(wine)
            .doesNotContain("FoSARDJERE")
    }
}
