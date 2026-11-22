package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la beautification des ingrédients par Levenshtein à l'Étape 5 ([Ocr5BeautifierIngredient]).
 */
class Ocr5BeautifierIngredientLevenshteinTest {

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
    fun testBeautifyIngredients_FixesTypoedIngredientNamesWithLevenshteinAtStep5() {
        val input = listOf(
            "100 g de parmnesan",
            "200 g de courjete",
            "1 poireau",
            "Sel"
        )

        val offTaxonomy = listOf("courgette", "parmesan")
        val result = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("La typo 'parmnesan' doit être corrigée en 'parmesan' à l'Étape 5")
            .that(result.any { it.contains("parmesan", ignoreCase = true) })
            .isTrue()

        assertWithMessage("La typo 'courjete' doit être corrigée en 'courgette' à l'Étape 5")
            .that(result.any { it.contains("courgette", ignoreCase = true) })
            .isTrue()

        assertWithMessage("Le mot valide 'poireau' doit rester inchangé")
            .that(result.any { it.contains("poireau", ignoreCase = true) })
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_MatchesTypoedWordAgainstOffTaxonomyList() {
        val input = listOf("1 petit pimnent")
        val offTaxonomy = listOf("piment", "piment d'Espelette", "piment doux")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("La typo 'pimnent' doit être corrigée en 'piment' depuis la liste de taxonomie OFF")
            .that(result)
            .containsExactly("1 petit piment")
    }
}
