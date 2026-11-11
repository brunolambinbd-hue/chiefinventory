package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la reconnaissance du verbe d'action 'empêcher' / 'éviter'
 * et la prévention de la pollution des lignes d'ingrédients.
 */
class OcrEmpecherVerbInstructionTest {

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
    fun testParse_EmpecherVerbInInstruction_DispatchesToInstructionsAndKeepsSaltClean() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val rawOcrText = """
            Salade d'avocats
            INGRÉDIENTS
            2 avocats
            1 citron
            sel et poivre
            Arrosez d'un peu du jus d'orange restant
            pour empêcher les avocats de noircir.
            Servez frais.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcrText, vocabulary)

        val ing = result.ingredients.orEmpty()
        val inst = result.instructions.orEmpty()

        assertWithMessage("L'ingrédient 'Sel et poivre' doit être préservé proprement")
            .that(ing)
            .contains("Sel et poivre")

        assertWithMessage("Les ingrédients ne doivent pas contenir le verbe d'instruction 'empêcher'")
            .that(ing)
            .doesNotContain("empêcher")

        assertWithMessage("La consigne 'pour empêcher les avocats de noircir' doit figurer dans les instructions")
            .that(inst)
            .contains("empêcher les avocats de noircir.")
    }
}
