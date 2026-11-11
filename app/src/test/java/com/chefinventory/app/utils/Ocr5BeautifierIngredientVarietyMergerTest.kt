package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Le qualificatif de variété orphelin 'princesses' doit être regroupé avec '200 g de haricots'")
            .that(result)
            .containsExactly(
                "200 g de haricots princesses",
                "100 g de sucre"
            )
            .inOrder()
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

        assertWithMessage("Le qualificatif majusculé 'Princesses' doit être passé en minuscules lors du regroupement")
            .that(result)
            .containsExactly(
                "200 g de haricots princesses",
                "100 g de sucre"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should return empty list for empty input`() {
        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(emptyList(), baseIngredients, qualifiers)
        assertWithMessage("Une liste d'ingrédients vide doit retourner une liste vide")
            .that(result)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should not merge if next line is a variety qualifier but current line is not base`() {
        val input = listOf(
            "200 g de farine",
            "princesses"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertWithMessage("L'ingrédient 'farine' qui n'attend pas de variété ne doit pas être regroupé avec 'princesses'")
            .that(result)
            .containsExactly(
                "200 g de farine",
                "princesses"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `beautifyIngredientVarieties should not merge if next line is not a variety qualifier`() {
        val input = listOf(
            "200 g de haricots",
            "200 g de farine"
        )

        val result = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(input, baseIngredients, qualifiers)

        assertWithMessage("Une ligne suivante ne correspondant pas à un qualificatif ne doit pas être regroupée")
            .that(result)
            .containsExactly(
                "200 g de haricots",
                "200 g de farine"
            )
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isBaseExpectingVariety should identify base ingredients`() {
        assertWithMessage("La ligne '200 g de haricots' doit être reconnue comme ingrédient de base attendant une variété")
            .that(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("200 g de haricots", baseIngredients))
            .isTrue()

        assertWithMessage("Le mot seul 'haricots' doit être reconnu comme ingrédient de base")
            .that(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("haricots", baseIngredients))
            .isTrue()

        assertWithMessage("La ligne '200 g de farine' ne doit pas être reconnue comme ingrédient de base pour variété")
            .that(Ocr5BeautifierIngredientVarietyMerger.isBaseExpectingVariety("200 g de farine", baseIngredients))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isVarietyQualifier should identify variety qualifiers`() {
        assertWithMessage("Le qualificatif 'princesses' doit être reconnu")
            .that(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("princesses", qualifiers))
            .isTrue()

        assertWithMessage("Le qualificatif complexe 'frais et bio' doit être reconnu")
            .that(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("frais et bio", qualifiers))
            .isTrue()

        assertWithMessage("Le qualificatif avec puce '• princesses' doit être reconnu")
            .that(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("• princesses", qualifiers))
            .isTrue()

        assertWithMessage("Le mot d'ingrédient 'farine' ne doit pas être reconnu comme qualificatif de variété")
            .that(Ocr5BeautifierIngredientVarietyMerger.isVarietyQualifier("farine", qualifiers))
            .isFalse()
    }
}
