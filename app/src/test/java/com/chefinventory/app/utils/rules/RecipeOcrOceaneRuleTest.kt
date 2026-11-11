package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class RecipeOcrOceaneRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `exhaustive reference test for recipe 03 salade oceane`() {
        val ocrText = """
            INGRÉDIENTS
            POUR
            4 PERSONNES
            250 g de crevettes
            grises décortiquées
            4 petites tomates
            (ou 2 grosses)
            l beau chicon
            I2 salade frisée
            l orange
            250 g de yaourt nature
            2 c. à soupe de
            mayonnaise
            2 c. à soupe de ketchup
            lc.à soupe de
            ciboulette hachée
            Tabasco
            sel et poivre
            Salade océane
            • Prélevez finement le zeste de
            l'orange, taillez-le en fins bâton-
            nets que vous plongez 1 minute
            dans un fond d'eau bouillante.
            Rafraîchissez à l'eau froide et lais-
            sez bien égoutter.
            • Mélangez le yaourt, le ketchup,
            la mayonnaise et la ciboulette.
            Ajoutez du Tabasco selon vos
            goûts, salez et poivrez. Réservez.
            • Lavez et essorez la salade, ajou-
            tez-y le chicon nettoyé et émincé.
            Disposez sur de grandes assiettes
            et saupoudrez de zeste d'orange.
            •Coupez les tomates en huit,
            répartissez-les sur les assiettes et
            garnissez le centre de crevettes
            grises.
            • Servez frais (ne laissez pas trop
            longtemps en attente); présentez la
            sauce à part.
            VIN CONSEILLĖ
            Touraine,
            Sauvignon blanc, 1995, 75 cl
            TOCRAINE
            SAETICsox
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val title = result.title ?: ""
        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""
        val wine = result.wine ?: ""

        // ==========================================
        // 1. VALIDATION ET EXCLUSIVITÉ DU TITRE
        // ==========================================
        assertWithMessage("Le titre extrait doit contenir 'Salade océane'")
            .that(title)
            .ignoringCase()
            .contains("Salade océane")

        assertWithMessage("Le titre ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Salade océane")

        assertWithMessage("Le titre ne doit pas fuiter dans le vin")
            .that(wine)
            .ignoringCase()
            .doesNotContain("Salade océane")

        // ==========================================
        // 2. VALIDATION ET EXCLUSIVITÉ DES INGRÉDIENTS
        // ==========================================
        val expectedIngredientsWithQty = listOf(
            "250 g de crevettes grises décortiquées",
            "4 petites tomates (ou 2 grosses)",
            "1 beau chicon",
            "1/2 salade frisée",
            "1 orange",
            "250 g de yaourt nature",
            "2 c. à soupe de mayonnaise",
            "2 c. à soupe de ketchup",
            "1 c. à soupe de ciboulette hachée"
        )

        for (item in expectedIngredientsWithQty) {
            assertWithMessage("L'ingrédient '$item' doit figurer dans les ingrédients")
                .that(ing)
                .ignoringCase()
                .contains(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans les instructions")
                .that(inst)
                .ignoringCase()
                .doesNotContain(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans le titre")
                .that(title)
                .ignoringCase()
                .doesNotContain(item)

            assertWithMessage("L'ingrédient '$item' ne doit pas fuiter dans le vin")
                .that(wine)
                .ignoringCase()
                .doesNotContain(item)
        }

        assertWithMessage("L'ingrédient 'Tabasco' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("Tabasco")

        assertWithMessage("L'assaisonnement 'sel et poivre' doit figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .contains("sel et poivre")

        // ==========================================
        // 3. VALIDATION ET EXCLUSIVITÉ DES INSTRUCTIONS
        // ==========================================
        val expectedInstructions = listOf(
            "Prélevez finement le zeste de l'orange, taillez-le en fins bâtonnets que vous plongez 1 minute dans un fond d'eau bouillante.",
            "Rafraîchissez à l'eau froide et laissez bien égoutter.",
            "Mélangez le yaourt, le ketchup",
            "la mayonnaise et la ciboulette.",
            "Ajoutez du Tabasco selon vos goûts, salez et poivrez.",
            "Réservez.",
            "Lavez et essorez la salade, ajoutez-y le chicon nettoyé et émincé.",
            "Disposez sur de grandes assiettes et saupoudrez de zeste d'orange.",
            "Coupez les tomates en huit.",
            "Garnissez le centre de crevettes grises.",
            "Servez frais (ne laissez pas trop longtemps en attente); présentez la sauce à part."
        )

        for (line in expectedInstructions) {
            assertWithMessage("La consigne '$line' doit figurer dans les instructions")
                .that(inst)
                .ignoringCase()
                .contains(line)

            assertWithMessage("La consigne '$line' ne doit pas fuiter dans les ingrédients")
                .that(ing)
                .ignoringCase()
                .doesNotContain(line)
        }

        // ==========================================
        // 4. METADONNÉES (Portions, Vin)
        // ==========================================
        assertWithMessage("Le nombre de portions doit être de 4")
            .that(result.servings)
            .isEqualTo("4")

        assertWithMessage("Le vin doit contenir 'Touraine'")
            .that(wine)
            .ignoringCase()
            .contains("Touraine")

        assertWithMessage("Le vin doit contenir 'Sauvignon blanc'")
            .that(wine)
            .ignoringCase()
            .contains("Sauvignon blanc")

        assertWithMessage("Le vin ne doit pas fuiter dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Touraine")

        assertWithMessage("Le vin ne doit pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Touraine")
    }
}
