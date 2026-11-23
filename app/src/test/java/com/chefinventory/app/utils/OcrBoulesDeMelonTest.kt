package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que '1/2 boîte de boules de melon au sirop' conserve 'boules' intact sans déviation Levenshtein en 'moules'.
 */
class OcrBoulesDeMelonTest {

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
    fun testBeautifyIngredients_PreservesDouzaineDeBoulesWithoutMoules() {
        val input = listOf(
            "1/2 boîte de boules de melon au sirop",
            "une douzaine de boules de melon",
            "1 douzaine de boules de glace",
            "10 boules de mozzarella"
        )
        val offTaxonomy = listOf("moules", "melon", "boîte", "glace", "mozzarella")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input, offTaxonomyList = offTaxonomy)

        assertWithMessage("Le mot 'boules' précédé de 'douzaine' ou 'boîte' ne doit JAMAIS être dévié par Levenshtein en 'moules'")
            .that(beautified)
            .containsExactly(
                "1/2 boîte de boules de melon au sirop",
                "Une douzaine de boules de melon",
                "1 douzaine de boules de glace",
                "10 boules de mozzarella"
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_ContextualDisambiguation_ConvertsBoulesToMoulesInMusselContext() {
        val musselInputs = listOf(
            "une douzaine de boules de Zélande",
            "1 kg de boules de bouchot",
            "boules marinières"
        )
        val offTaxonomy = listOf("moules", "moules de Zélande", "moules de bouchot")

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(musselInputs, offTaxonomyList = offTaxonomy)

        assertWithMessage("Dans le contexte d'appellations de moules (Zélande, bouchot, marinière), 'boules' doit être corrigé en 'moules'")
            .that(beautified)
            .containsExactly(
                "Une douzaine de moules de Zélande",
                "1 kg de moules de bouchot",
                "Moules marinières"
            )
    }
}
