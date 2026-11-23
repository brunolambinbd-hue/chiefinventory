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
 * Suite de tests unitaires TDD d'isolation vérifiant que 'Votre marché: • 2 bottes d'asperges • 200 g de crevettes grises' est correctement scindé sur la puce interne • en 2 ingrédients distincts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrBulletSplitTest {

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
    fun testSplitCombinedIngredients_BulletSeparatedLine_SplitsIntoDistinctIngredients() {
        val rawLine = "Votre marché: • 2 bottes d'asperges • 200 g de crevettes grises"

        val splits = Ocr4MergerIngredientSplitter.splitCombinedIngredients(
            line = rawLine,
            commonItems = vocabulary.commonIngredientsNoQty,
            mandatoryComplements = vocabulary.mandatoryComplementIngredients,
            preparationModifiers = vocabulary.ingredientPreparationModifiers,
            offTaxonomyList = listOf("asperges", "crevettes grises")
        )

        assertWithMessage("La ligne avec puce interne • doit être scindée en 2 ingrédients distincts ('2 bottes d'asperges' et '200 g de crevettes grises')")
            .that(splits)
            .containsExactly("2 bottes d'asperges", "200 g de crevettes grises")
            .inOrder()
    }
}
