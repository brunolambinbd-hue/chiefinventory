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
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 3 (Aiguilleur Ocr3CategorizerRouter),
 * le routeur s'adapte dynamiquement à la densité 2D et préserve les sections d'ingrédients et d'instructions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerRouterMultiColumnTest {

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
    fun testRouteAndCategorize_MultiColumnMelonRecipe_ExtractsRawSectionsCleanly() {
        val lines2D = listOf(
            OcrTextLine("Büchettes de melon", top = 608, left = 454, bottom = 778, right = 1392),
            OcrTextLine("au jambon italien et au porto", top = 740, left = 672, bottom = 865, right = 1827),
            OcrTextLine("INGRÉDIENTS", top = 86, left = 114, bottom = 122, right = 380),
            OcrTextLine("1 melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon italien", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("• Pelez le melon, épépinez-le et", top = 910, left = 93, bottom = 951, right = 623),
            OcrTextLine("• Mixez le porto, le vinaigre, le", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781),
            OcrTextLine("Atinum, Bordeaux Graves", top = 1228, left = 1396, bottom = 1264, right = 1778)
        )

        val rawLines = lines2D.map { it.text }
        val titleResult = Ocr3CategorizerTitleExtractor.extractTitle(rawLines, vocabulary, lines2D)
        val sections = Ocr3CategorizerRouter.routeAndCategorize(titleResult.cleanedLines, lines2D, rawLines, vocabulary)

        assertWithMessage("Les ingrédients bruts extraits à l'Étape 3 doivent contenir '1 melon Galia'")
            .that(sections.rawIngredientsList)
            .contains("1 melon Galia")

        assertWithMessage("Les instructions brutes extraites à l'Étape 3 doivent contenir 'Pelez le melon...'")
            .that(sections.rawInstructionsList)
            .contains("Pelez le melon, épépinez-le et")

        assertWithMessage("Le vin doit être capturé dans les méta-données ou la section vin")
            .that(sections.detectedWineList)
            .isNotEmpty()
    }
}
