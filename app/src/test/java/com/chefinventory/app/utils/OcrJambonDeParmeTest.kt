package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que 'jambon de Parme' ne subit aucune déviation Levenshtein vers 'jambon de palme'.
 */
class OcrJambonDeParmeTest {

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
    fun testBeautifyIngredients_PreservesJambonDeParmeWithoutPalme() {
        val input = listOf("2 ou 3 paquets de jambon de Parme en fines tranches (125 à 180 g)")
        val offTaxonomy = listOf("palme", "jambon", "jambon cuit")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("Le mot 'Parme' de 'jambon de Parme' ne doit JAMAIS être dévié par Levenshtein en 'palme'")
            .that(beautified)
            .containsExactly("2 ou 3 paquets de jambon de Parme en fines tranches (125 à 180 g)")
    }
}
