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
 * Suite de tests unitaires TDD d'isolation vérifiant qu'un nombre pur 2D isolé sans parenthèse (ex: '20', '15', '30')
 * est capturé en amont comme temps de préparation (prepTime) avant que Ocr2Cleaner ne le supprime en tant que numéro de page.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2DUpstreamPureNumberTimeTest {

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
    fun testRecipeOcrParser_PureStandaloneNumber2D_PreExtractedAsUpstreamPrepTime() {
        val lines2D = listOf(
            OcrTextLine("Tarlettes aux fraises", top = 50, left = 100, bottom = 90, right = 500),
            OcrTextLine("20", top = 850, left = 1282, bottom = 891, right = 1350),
            OcrTextLine("Ingrédients pour 4 personnes", top = 200, left = 100, bottom = 230, right = 400),
            OcrTextLine("250 g de fraises", top = 240, left = 100, bottom = 270, right = 400),
            OcrTextLine("1 pâte sablée", top = 280, left = 100, bottom = 310, right = 400),
            OcrTextLine("Faites cuire la pâte à blanc pendant 15 minutes.", top = 400, left = 100, bottom = 430, right = 600)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary)

        assertWithMessage("Le nombre pur isolé '20' scanné en 2D doit être pré-extrait comme temps de préparation")
            .that(result.prepTime)
            .isEqualTo("20")

        assertWithMessage("Les ingrédients ne doivent pas contenir le nombre '20'")
            .that(result.ingredients)
            .doesNotContain("20")
    }
}
