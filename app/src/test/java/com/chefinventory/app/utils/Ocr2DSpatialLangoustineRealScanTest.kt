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
 * Suite de tests unitaires TDD d'isolation basée à 100% sur le Logcat 2D réel scanné par ML Kit pour la recette 'Salade de langoustines au curry'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DSpatialLangoustineRealScanTest {

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
    fun testRecipeOcrParser_Real2DScanLogcat_ExtractsCorrectTitleIngredientsAndInstructions() {
        val lines2D = listOf(
            OcrTextLine("re", top = 22, left = 50, bottom = 53, right = 167),
            OcrTextLine("35", top = 394, left = 92, bottom = 430, right = 121),
            OcrTextLine("e", top = 21, left = 348, bottom = 49, right = 372),
            OcrTextLine("Ingrédients pour 4 personnes", top = 107, left = 197, bottom = 145, right = 613),
            OcrTextLine("24 langoustines cuites", top = 172, left = 197, bottom = 203, right = 512),
            OcrTextLine("I mangueà point", top = 213, left = 198, bottom = 244, right = 456),
            OcrTextLine("4 c. à soupe de petits croûtons", top = 252, left = 197, bottom = 285, right = 621),
            OcrTextLine("3 c. à soupe de mayonnaise", top = 295, left = 214, bottom = 327, right = 584),
            OcrTextLine("lc. à café de currTy, 1/2 citron", top = 335, left = 197, bottom = 367, right = 620),
            OcrTextLine("I yaourt nature (I25 g)", top = 376, left = 213, bottom = 408, right = 536),
            OcrTextLine("4 belles feuilles de salade", top = 417, left = 197, bottom = 449, right = 556),
            OcrTextLine("cresson, sel et poivre", top = 459, left = 197, bottom = 490, right = 491),
            OcrTextLine("126", top = 19, left = 740, bottom = 62, right = 835),
            OcrTextLine("alade de langoustines au curry", top = 773, left = 246, bottom = 912, right = 1696),
            OcrTextLine("Délayez le curry avec la mayonnaise, puis ajoutez. le yaourt et", top = 935, left = 152, bottom = 967, right = 982),
            OcrTextLine("un trait de jus de citron. Salez et poivrez; réservez.", top = 974, left = 151, bottom = 1004, right = 772),
            OcrTextLine("Décortiquez les langoustines. Réservez4 belles têtes.", top = 1015, left = 150, bottom = 1054, right = 802),
            OcrTextLine("Pelez la mangue, taillez-la en fines lamelles. Lavez et essorez la sala-", top = 1065, left = 150, bottom = 1102, right = 983),
            OcrTextLine("de; lavez le cresson.", top = 1113, left = 149, bottom = 1139, right = 398),
            OcrTextLine("Disposez six langoustines sur chaque assiette, ajoutez tout autour des", top = 1151, left = 150, bottom = 1192, right = 981),
            OcrTextLine("lamelles de mangue. Décorez de salade et de cresson, ajoutez les", top = 1190, left = 149, bottom = 1228, right = 981),
            OcrTextLine("croûtons.", top = 1235, left = 147, bottom = 1262, right = 258),
            OcrTextLine("Disposez une tête de langoustine sur chaque assiette, nappez les lan-", top = 1277, left = 148, bottom = 1315, right = 979),
            OcrTextLine("goustines d'un peu de sauce au curry.", top = 1320, left = 146, bottom = 1354, right = 606),
            OcrTextLine("Servez frais, en présentant le restant de la sauce à part.", top = 1363, left = 148, bottom = 1402, right = 815),
            OcrTextLine("Vous pouvez préparer une soupe avec les têtes et les carcasses des lan-", top = 1408, left = 149, bottom = 1453, right = 980),
            OcrTextLine("goustines.", top = 1459, left = 148, bottom = 1492, right = 271),
            OcrTextLine("axperrEAa", top = 1389, left = 1172, bottom = 1403, right = 1251),
            OcrTextLine("Vin conseillé", top = 1222, left = 1676, bottom = 1255, right = 1912),
            OcrTextLine("Gewurztraminer,", top = 1285, left = 1683, bottom = 1319, right = 1913),
            OcrTextLine("Caves de Pfaffenhein", top = 1328, left = 1625, bottom = 1354, right = 1916),
            OcrTextLine("MO, 1992, 75 cl", top = 1367, left = 1696, bottom = 1395, right = 1917)
        )

        val offTaxonomy = listOf("langoustines", "mangue", "mayonnaise", "curry", "citron", "yaourt", "salade", "cresson", "sauce au curry")
        val result = RecipeOcrParser.parse(lines2D, vocabulary, offTaxonomyList = offTaxonomy)

        assertWithMessage("Le titre extrait du scan 2D réel doit être 'Salade de langoustines au curry'")
            .that(result.title)
            .ignoringCase()
            .isEqualTo("Salade de langoustines au curry")

        assertWithMessage("La liste des ingrédients ne doit PAS contenir de faux ingrédient 'goustines' ou 'sauce au curry' volé des instructions")
            .that(result.ingredients)
            .doesNotContain("Goustines")

        assertWithMessage("L'instruction césurée 'nappez les lan-' + 'goustines...' doit être fusionnée en 1 seule ligne d'instruction complète")
            .that(result.instructions)
            .contains("Disposez une tête de langoustine sur chaque assiette, nappez les langoustines d'un peu de sauce au curry.")

        assertWithMessage("L'instruction tronquée par le point parasite 'ajoutez.' doit être fusionnée sans coupure")
            .that(result.instructions)
            .contains("Délayez le curry avec la mayonnaise, puis ajoutez le yaourt et un trait de jus de citron.")
    }
}
