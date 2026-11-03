package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr5BeautifierIngredientVarietyMerger].
 */
class Ocr5BeautifierIngredientVarietyMergerTest {

    private val baseIngredients = listOf("haricots", "crevettes", "poivrons", "chou", "yaourt", "saumon")
    private val qualifiers = listOf("princesses", "verts", "grises", "roses", "grec", "nature", "frais", "décortiquées")

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should merge orphan variety qualifier with preceding base ingredient`() {
        val input = listOf(
            "200 g de haricots",
            "princesses",
            "100 g de sucre"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "200 g de haricots princesses",
            "100 g de sucre"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should uncapitalize capitalized variety qualifier when merging`() {
        val input = listOf(
            "200 g de haricots",
            "Princesses",
            "100 g de sucre"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "200 g de haricots princesses",
            "100 g de sucre"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should return empty list for empty input`() {
        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(emptyList(), baseIngredients, qualifiers)
        assertThat(result).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should not merge if next line is a variety qualifier but current line is not base`() {
        val input = listOf(
            "200 g de farine",
            "princesses"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "200 g de farine",
            "princesses"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should not merge if next line is not a variety qualifier`() {
        val input = listOf(
            "200 g de haricots",
            "200 g de farine"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertThat(result).containsExactly(
            "200 g de haricots",
            "200 g de farine"
        ).inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isBaseExpectingVariety should identify base ingredients`() {
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("200 g de haricots", baseIngredients)).isTrue()
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("haricots", baseIngredients)).isTrue()
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("200 g de farine", baseIngredients)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isVarietyQualifier should identify variety qualifiers`() {
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("princesses", qualifiers)).isTrue()
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("frais et bio", qualifiers)).isTrue()
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("• princesses", qualifiers)).isTrue()
        assertThat(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("farine", qualifiers)).isFalse()
    }
}
