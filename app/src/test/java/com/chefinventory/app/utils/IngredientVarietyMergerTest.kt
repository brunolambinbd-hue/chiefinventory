package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le réassemblage dynamique des variétés d'ingrédients.
 */
class IngredientVarietyMergerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredientVarieties_UppercaseAndBaseEndings() {
        val baseIngredients = listOf("Haricots", "Pomme de terre")
        val qualifiers = listOf("Princesses", "Rissolées")

        val input = listOf(
            "200 g de Haricots",
            "Princesses",
            "500 g de Pomme de terre",
            "Rissolées"
        )

        val result = IngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).contains("200 g de Haricots princesses")
        assertThat(result).contains("500 g de Pomme de terre rissolées")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should merge split variety qualifiers with base ingredients`() {
        val baseIngredients = vocabulary.baseIngredientsExpectingVariety
        val qualifiers = vocabulary.ingredientVarietyQualifiers

        val input = listOf(
            "250 g de crevettes",
            "grises décortiquées",
            "8 tranches de jambon",
            "italien",
            "250 g de yaourt",
            "nature",
            "200 g de poivrons",
            "rouges",
            "200 g de saumon",
            "fumé",
            "500 g de pommes de terre",
            "100 g de beurre"
        )

        val result = IngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "250 g de crevettes grises décortiquées",
            "8 tranches de jambon italien",
            "250 g de yaourt nature",
            "200 g de poivrons rouges",
            "200 g de saumon fumé",
            "500 g de pommes de terre",
            "100 g de beurre"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should leave unrelated ingredients unchanged`() {
        val baseIngredients = vocabulary.baseIngredientsExpectingVariety
        val qualifiers = vocabulary.ingredientVarietyQualifiers

        val input = listOf(
            "150 g de beurre",
            "2 gousses d'ail",
            "Sel et poivre"
        )

        val result = IngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "150 g de beurre",
            "2 gousses d'ail",
            "Sel et poivre"
        ).inOrder()
    }
}
