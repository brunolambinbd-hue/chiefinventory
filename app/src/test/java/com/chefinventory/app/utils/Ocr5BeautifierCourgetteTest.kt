package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la correction Levenshtein d'Étape 5 pour le mot 'courgette' via la liste OFF de Room DB ([Ocr5BeautifierIngredient]).
 */
class Ocr5BeautifierCourgetteTest {

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
    fun testBeautifyIngredients_FixesCourjeteTypoUsingOffTaxonomyList() {
        val input = listOf("200 g de courjete")
        val offTaxonomy = listOf("courgette", "courgettes")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(
            ingredients = input,
            offTaxonomyList = offTaxonomy
        )

        assertWithMessage("La typo 'courjete' doit être corrigée en 'courgette' via la liste OFF sans présence dans le XML")
            .that(result)
            .containsExactly("200 g de courgette")
    }
}
