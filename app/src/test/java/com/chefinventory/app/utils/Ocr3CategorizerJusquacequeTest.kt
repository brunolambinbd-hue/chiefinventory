package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la validation de l'aiguillage de la locution de transition
 * "Jusqu'à ce que" vers les instructions sans polluer la liste des ingrédients.
 */
class Ocr3CategorizerJusquacequeTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParse_JusquacequeLine_DispatchesToInstructionsAndExcludesFromIngredients() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            Tarte aux pommes
            INGRÉDIENTS
            200 g de farine
            100 g de beurre
            sel et poivre
            Faites cuire la pâte au four.
            Jusqu'à ce que
            la croûte soit bien dorée.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val ing = result.ingredients.orEmpty()
        val inst = result.instructions.orEmpty()

        assertWithMessage("La ligne 'Jusqu'à ce que' ne doit PAS être classée comme un ingrédient")
            .that(ing)
            .doesNotContain("Jusqu'à ce que")

        assertWithMessage("La ligne 'Jusqu'à ce que' ne doit pas figurer dans les ingrédients (minuscules)")
            .that(ing.lowercase())
            .doesNotContain("jusqu'à ce que")

        assertWithMessage("La consigne 'Jusqu'à ce que la croûte soit bien dorée.' doit figurer dans les instructions")
            .that(inst)
            .contains("Jusqu'à ce que la croûte soit bien dorée.")
    }
}
