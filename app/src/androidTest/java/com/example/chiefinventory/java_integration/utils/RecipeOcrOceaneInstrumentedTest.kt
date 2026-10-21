package com.example.chiefinventory.java_integration.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for Recipe 03 (Salade océane) running with real Android project Resources on device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class RecipeOcrOceaneInstrumentedTest {

    @Test
    fun testScenarioSaladeOceane() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val res = appContext.resources

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

        val result = RecipeOcrParser.parse(ocrText, res)

        val title = result.title ?: ""
        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""
        val wine = result.wine ?: ""

        // ==========================================
        // 1. VALIDATION ET EXCLUSIVITÉ DU TITRE
        // ==========================================
        assertThat(title).ignoringCase().contains("Salade océane")
        assertThat(ing).ignoringCase().doesNotContain("Salade océane")
        assertThat(wine).ignoringCase().doesNotContain("Salade océane")

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
            assertThat(ing).ignoringCase().contains(item)
            assertThat(inst).ignoringCase().doesNotContain(item)
            assertThat(title).ignoringCase().doesNotContain(item)
            assertThat(wine).ignoringCase().doesNotContain(item)
        }

        assertThat(ing).ignoringCase().contains("Tabasco")
        assertThat(ing).ignoringCase().contains("sel et poivre")

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
            assertThat(inst).ignoringCase().contains(line)
            assertThat(ing).ignoringCase().doesNotContain(line)
        }

        // ==========================================
        // 4. METADONNÉES (Portions, Vin)
        // ==========================================
        assertThat(result.servings).isEqualTo("4")

        assertThat(wine).ignoringCase().contains("Touraine")
        assertThat(wine).ignoringCase().contains("Sauvignon blanc")
        assertThat(ing).ignoringCase().doesNotContain("Touraine")
        assertThat(inst).ignoringCase().doesNotContain("Touraine")
    }
}
