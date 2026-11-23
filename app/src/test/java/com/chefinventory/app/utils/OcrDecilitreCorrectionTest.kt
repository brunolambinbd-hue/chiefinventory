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
 * Suite de tests unitaires TDD d'isolation vérifiant que '10 d de' et '20 d de' sont corrigés en '10 dl de' et '20 dl de' et que la ligne multi-ingrédients est correctement scindée.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrDecilitreCorrectionTest {

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
    fun testCorrectionAndSplit_10dDeCremeFraiche_CorrectsDlAndSplitsIngredients() {
        val rawInput = "• 10 d de crème fraîche 20 d de yaourt persil ou ciboulette"
        val correctedText = Ocr1Corrector.correct(rawInput)

        assertWithMessage("La chaîne '10 d de' doit être corrigée en '10 dl de' et '20 d de' en '20 dl de'")
            .that(correctedText)
            .contains("10 dl de crème fraîche 20 dl de yaourt")

        val mergedList = Ocr4Merger.mergeIngredients(
            rawIngredients = listOf(correctedText),
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            commonIngredients = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            customConnectors = vocabulary.ingredientConnectors,
            offTaxonomyList = listOf("crème fraîche", "yaourt", "persil", "ciboulette")
        )

        assertWithMessage("La ligne multi-ingrédients doit être découpée en ingrédients distincts (10 dl de crème fraîche, 20 dl de yaourt...)")
            .that(mergedList)
            .containsAtLeast("10 dl de crème fraîche", "20 dl de yaourt")
    }
}
