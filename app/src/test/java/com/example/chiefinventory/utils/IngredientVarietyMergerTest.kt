package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la détection et la fusion des variétés d'ingrédients
 * scindées sur deux lignes adjacentes par [IngredientVarietyMerger].
 */
class IngredientVarietyMergerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `isBaseExpectingVariety should return true for base ingredients expecting variety`() {
        val baseIngredients = listOf("haricots", "crevettes", "jambon", "yaourt", "poivrons", "saumon", "pommes")

        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("200 g de haricots", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("250 g de crevettes", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("8 tranches de jambon", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("yaourt", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("200 g de poivrons", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("200 g de saumon", baseIngredients)).isTrue()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("500 g de pommes", baseIngredients)).isTrue()

        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("200 g de farine", baseIngredients)).isFalse()
        assertThat(IngredientVarietyMerger.isBaseExpectingVariety("sel et poivre", baseIngredients)).isFalse()
    }

    @Test
    fun `isVarietyQualifier should return true for variety qualifiers`() {
        val qualifiers = listOf("princesses", "verts", "grises", "italien", "nature", "grec", "rouges", "fumé", "de terre")

        assertThat(IngredientVarietyMerger.isVarietyQualifier("princesses", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("Italien", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("grises décortiquées", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("grec", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("Nature", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("Rouges", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("Fumé", qualifiers)).isTrue()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("De terre", qualifiers)).isTrue()

        assertThat(IngredientVarietyMerger.isVarietyQualifier("poivrons", qualifiers)).isFalse()
        assertThat(IngredientVarietyMerger.isVarietyQualifier("beurre", qualifiers)).isFalse()
    }

    @Test
    fun `beautifyIngredientVarieties should merge base ingredients with variety qualifiers on adjacent lines`() {
        val baseIngredients = vocabulary.baseIngredientsExpectingVariety
        val qualifiers = vocabulary.ingredientVarietyQualifiers

        val input = listOf(
            "200 g de haricots",
            "Princesses",
            "250 g de crevettes",
            "Grises décortiquées",
            "8 tranches de jambon",
            "Italien",
            "250 g de yaourt",
            "Nature",
            "200 g de poivrons",
            "Rouges",
            "200 g de saumon",
            "Fumé",
            "500 g de pommes",
            "De terre",
            "100 g de beurre"
        )

        val result = IngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "200 g de haricots princesses",
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
