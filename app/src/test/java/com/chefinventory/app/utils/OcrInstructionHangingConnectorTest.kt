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
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 3, une ligne se terminant par une préposition suspendue ('sur', 'dans', 'avec')
 * verrouille la ligne suivante dans les instructions, empêchant ainsi le vol de la ligne vers les ingrédients si elle contient un mot-clé comme 'vinaigrette'.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrInstructionHangingConnectorTest {

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
    fun testCategorize_HangingPrepositionSur_LocksNextLineInInstructions() {
        val lines = listOf(
            "INGRÉDIENTS",
            "1 melon Galia",
            "4 c. à soupe de porto",
            "sel et poivre",
            "INSTRUCTIONS",
            "• Disposez les bûchettes sur",
            "une assiette avec la vinaigrette.",
            "Décorez de brins d'aneth."
        )

        val result = RecipeOcrParser.parse(lines, vocabulary, offTaxonomyList = listOf("melon", "porto", "vinaigrette", "aneth"))

        assertWithMessage("La ligne 'une assiette avec la vinaigrette.' ne doit PAS être volée dans les ingrédients")
            .that(result.ingredients)
            .doesNotContain("assiette avec la vinaigrette")

        assertWithMessage("Les consignes doivent fusionner la suite de la préposition 'sur'")
            .that(result.instructions)
            .contains("Disposez les bûchettes sur une assiette avec la vinaigrette.")
    }
}
