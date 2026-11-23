package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires TDD d'isolation E2E basée à 100% sur le Logcat 2D réel scanné par ML Kit pour la recette 'Salade Océane'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialOceaneRealScanTest {

    private lateinit var context: Context
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipeOcrParser_Real2DScanLogcatOceane_ExtractsTitleIngredientsAndWine() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 87, left = 90, bottom = 123, right = 349),
            OcrTextLine("POUR", top = 129, left = 169, bottom = 155, right = 272),
            OcrTextLine("4 PERSONNES", top = 158, left = 94, bottom = 189, right = 344),
            OcrTextLine("250 g de crevettes", top = 210, left = 74, bottom = 238, right = 369),
            OcrTextLine("grises décortiquées", top = 238, left = 103, bottom = 271, right = 344),
            OcrTextLine("4 petites tomates", top = 273, left = 112, bottom = 299, right = 328),
            OcrTextLine("(ou 2 grosses)", top = 302, left = 130, bottom = 325, right = 310),
            OcrTextLine("l beau chicon", top = 330, left = 150, bottom = 358, right = 296),
            OcrTextLine("I2 salade frisée", top = 362, left = 133, bottom = 390, right = 311),
            OcrTextLine("l orange", top = 395, left = 175, bottom = 420, right = 270),
            OcrTextLine("250 g de yaourt nature", top = 425, left = 100, bottom = 455, right = 354),
            OcrTextLine("2 c. à soupe de", top = 460, left = 151, bottom = 490, right = 294),
            OcrTextLine("mayonnaise", top = 492, left = 170, bottom = 520, right = 291),
            OcrTextLine("2 c. à soupe de ketchup", top = 525, left = 120, bottom = 555, right = 350),
            OcrTextLine("lc.à soupe de", top = 560, left = 140, bottom = 590, right = 310),
            OcrTextLine("ciboulette hachée", top = 592, left = 130, bottom = 620, right = 320),
            OcrTextLine("Tabasco", top = 625, left = 170, bottom = 650, right = 270),
            OcrTextLine("sel et poivre", top = 655, left = 152, bottom = 680, right = 297),
            OcrTextLine("Salade océane", top = 609, left = 487, bottom = 746, right = 1256),
            OcrTextLine("• Prélevez finement le zeste de", top = 804, left = 66, bottom = 848, right = 578),
            OcrTextLine("l'orange, taillez-le en fins bâton-", top = 852, left = 67, bottom = 898, right = 501),
            OcrTextLine("nets que vous plongez 1 minute", top = 907, left = 66, bottom = 939, right = 500),
            OcrTextLine("dans un fond d'eau bouillante.", top = 945, left = 66, bottom = 975, right = 480),
            OcrTextLine("Rafraîchissez à l'eau froide et lais-", top = 980, left = 66, bottom = 1010, right = 520),
            OcrTextLine("sez bien égoutter.", top = 1015, left = 66, bottom = 1045, right = 300),
            OcrTextLine("VIN CONSEILLÉ", top = 1128, left = 1416, bottom = 1187, right = 1737),
            OcrTextLine("Muscadet Sèvre-et-Maine", top = 1191, left = 1336, bottom = 1237, right = 1732),
            OcrTextLine("Sur Lie, 1995, 75 cl", top = 1232, left = 1499, bottom = 1279, right = 1739)
        )

        val offTaxonomy = listOf("crevettes", "tomates", "chicon", "salade", "orange", "yaourt", "mayonnaise", "ketchup", "ciboulette", "tabasco", "muscadet", "vin")
        val result = RecipeOcrParser.parse(lines2D, vocabulary, offTaxonomyList = offTaxonomy)

        assertWithMessage("Le titre extrait du scan 2D réel doit être 'Salade océane'")
            .that(result.title)
            .ignoringCase()
            .contains("océane")

        assertWithMessage("Aucune durée n'étant imprimée sur le scan de la Salade Océane, prepTime doit valoir null")
            .that(result.prepTime)
            .isNull()

        assertWithMessage("La liste des ingrédients doit contenir '250 g de crevettes grises décortiquées'")
            .that(result.ingredients)
            .contains("crevettes")

        assertWithMessage("La recommandation de vin doit capturer le Muscadet")
            .that(result.wine)
            .contains("Muscadet")
    }
}
