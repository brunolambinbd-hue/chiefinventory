package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr5BeautifierInstruction.fixFrenchInstructionElisions].
 */
class OcrFrenchInstructionElisionTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFixFrenchInstructionElisions_RestoresMissingApostrophesInInstructionsOnlyForKnownIngredients() {
        val offTaxonomy = listOf("orange", "oignon", "épinard")
        val line1 = Ocr5BeautifierInstruction.fixFrenchInstructionElisions("Prélevez finement le zeste de lorange.", vocabulary, offTaxonomy)
        assertWithMessage("La typo 'lorange' pour l'ingrédient connu 'orange' doit être rétablie en 'l'orange'")
            .that(line1)
            .isEqualTo("Prélevez finement le zeste de l'orange.")

        val line2 = Ocr5BeautifierInstruction.fixFrenchInstructionElisions("Hachez loignon et les épinards.", vocabulary, offTaxonomy)
        assertWithMessage("La typo 'loignon' pour l'ingrédient connu 'oignon' doit être rétablie en 'l'oignon'")
            .that(line2)
            .isEqualTo("Hachez l'oignon et les épinards.")

        val line3 = Ocr5BeautifierInstruction.fixFrenchInstructionElisions("Lavez les légumes, essorez la laitue, taillez les lamelles.", vocabulary, offTaxonomy)
        assertWithMessage("Les mots réels 'légumes', 'laitue', 'lamelles' ne doivent PAS être déformés en 'l'égumes', 'l'aitue', 'l'amelles'")
            .that(line3)
            .isEqualTo("Lavez les légumes, essorez la laitue, taillez les lamelles.")
    }
}
