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
 * Suite de tests unitaires TDD d'isolation E2E basée à 100% sur le Logcat 2D réel scanné par ML Kit pour la recette 'Salade d'aubergines'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialAuberginesRealScanTest {

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
    fun testRecipeOcrParser_Real2DScanLogcatAubergines_ExtractsTitlePrepTimeIngredientsAndWine() {
        val lines2D = listOf(
            OcrTextLine("CONRAD|", top = 18, left = 33, bottom = 68, right = 242),
            OcrTextLine("BR LS", top = 54, left = 74, bottom = 86, right = 197),
            OcrTextLine("Nahit YILMAZ", top = 195, left = 290, bottom = 216, right = 479),
            OcrTextLine("Hôtel Conrad International", top = 246, left = 292, bottom = 266, right = 577),
            OcrTextLine("avenue Louise 7l", top = 274, left = 292, bottom = 298, right = 469),
            OcrTextLine("1050 BRUXELLES", top = 306, left = 295, bottom = 325, right = 491),
            OcrTextLine("Tél. : 02I542.42.42", top = 334, left = 295, bottom = 355, right = 496),
            OcrTextLine("saveur de", top = 255, left = 779, bottom = 341, right = 948),
            OcrTextLine("alade d'aubergines", top = 720, left = 228, bottom = 829, right = 1045),
            OcrTextLine("1 Plongez les aubergines dans I'eau bouillante et retirez la peau. Hachez la chair et faites-la suer dans", top = 917, left = 3, bottom = 971, right = 1177),
            OcrTextLine("un peu de beurre avec l'ail haché et le piment émincé fin. Assaisonnez. Ajoutez le jus de citron et", top = 950, left = 44, bottom = 1006, right = 1178),
            OcrTextLine("le persil haché. Servez frais, décorez de tomates et accompagnez de tranches de pain grillé.", top = 986, left = 44, bottom = 1038, right = 1098),
            OcrTextLine("23", top = 1345, left = 993, bottom = 1389, right = 1042),
            OcrTextLine("20", top = 850, left = 1282, bottom = 891, right = 1350),
            OcrTextLine("I995", top = 1221, left = 1355, bottom = 1234, right = 1374),
            OcrTextLine("Ingrédients pour4 personnes", top = 655, left = 1411, bottom = 691, right = 1801),
            OcrTextLine("6 aubergines moyennes (t 1 kg)", top = 713, left = 1413, bottom = 748, right = 1788),
            OcrTextLine("1 ou 2 citrons", top = 754, left = 1430, bottom = 780, right = 1575),
            OcrTextLine("150 g de beurre", top = 789, left = 1415, bottom = 819, right = 1597),
            OcrTextLine("I piment rouge", top = 826, left = 1432, bottom = 856, right = 1593),
            OcrTextLine("persil haché", top = 860, left = 1414, bottom = 892, right = 1561),
            OcrTextLine("2 gousses d'ail", top = 894, left = 1431, bottom = 930, right = 1593),
            OcrTextLine("sel, poivre", top = 934, left = 1418, bottom = 965, right = 1536),
            OcrTextLine("Pour la décoration: tomates", top = 967, left = 1418, bottom = 995, right = 1750),
            OcrTextLine("Notre vin conseillé", top = 1120, left = 1608, bottom = 1160, right = 1943),
            OcrTextLine("Rousse Région Merlot", top = 1174, left = 1685, bottom = 1207, right = 1945),
            OcrTextLine("(vin rouge de Bulgarie)", top = 1205, left = 1677, bottom = 1240, right = 1945),
            OcrTextLine("M.O.", top = 1252, left = 1890, bottom = 1274, right = 1948)
        )

        val offTaxonomy = listOf("aubergines", "citrons", "beurre", "piment rouge", "persil", "ail", "tomates", "merlot", "vin")
        val result = RecipeOcrParser.parse(lines2D, vocabulary, offTaxonomyList = offTaxonomy)

        assertWithMessage("Le titre extrait doit être 'Salade d'aubergines'")
            .that(result.title)
            .ignoringCase()
            .contains("aubergines")

        assertWithMessage("Le temps de préparation de 20 minutes doit être pré-extrait en amont du nombre pur isolé '20'")
            .that(result.prepTime)
            .isEqualTo("20")

        assertWithMessage("La liste des ingrédients doit contenir '6 aubergines moyennes'")
            .that(result.ingredients)
            .contains("aubergines")

        assertWithMessage("La liste des ingrédients doit contenir '150 g de beurre'")
            .that(result.ingredients)
            .contains("150 g de beurre")

        assertWithMessage("La recommandation de vin doit capturer le Merlot")
            .that(result.wine)
            .contains("Merlot")
    }
}
