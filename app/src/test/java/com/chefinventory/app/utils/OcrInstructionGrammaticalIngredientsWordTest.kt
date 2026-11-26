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
 * Suite de tests unitaires TDD d'isolation vérifiant que le mot 'ingrédients'
 * n'est jamais supprimé lorsqu'il est utilisé comme complément d'objet d'un verbe d'action à l'impératif dans une consigne d'instruction
 * (ex: 'Mélangez tous ces ingrédients dans un saladier').
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrInstructionGrammaticalIngredientsWordTest {

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
    fun testParse_InstructionWithGrammaticalIngredientsWord_RetainsWordIntact() {
        val lines2D = listOf(
            OcrTextLine("INGRÉDIENTS", top = 100, left = 100, bottom = 130, right = 300),
            OcrTextLine("200 g de chocolat", top = 140, left = 100, bottom = 170, right = 300),
            OcrTextLine("• Mélangez tous ces ingrédients dans un saladier.", top = 250, left = 100, bottom = 280, right = 600),
            OcrTextLine("• Incorporez les ingrédients un par un.", top = 290, left = 100, bottom = 320, right = 600)
        )

        val result = RecipeOcrParser.parse(lines2D, vocabulary, offTaxonomyList = listOf("chocolat"))

        assertWithMessage("La consigne doit conserver le mot 'ingrédients' intact après le verbe 'Mélangez'")
            .that(result.instructions)
            .contains("Mélangez tous ces ingrédients dans un saladier.")

        assertWithMessage("La consigne doit conserver le mot 'ingrédients' intact après le verbe 'Incorporez'")
            .that(result.instructions)
            .contains("Incorporez les ingrédients un par un.")
    }
}
