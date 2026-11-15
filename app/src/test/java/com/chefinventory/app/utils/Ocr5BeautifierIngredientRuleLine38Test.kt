package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau Étape 5 ([Ocr5BeautifierIngredient]) vérifiant spécifiquement
 * la règle de purge du bruit de lettre orpheline 'h' / 'H' en fin de mot d'au moins 3 lettres
 * `(?i)(?<=\b\p{L}{3,20}\s)[hH]$` sans impacter les terminaisons d'ingrédients français.
 */
class Ocr5BeautifierIngredientRuleLine38Test {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_RuleLine38_PurgesTrailingNoiseH_AndPreservesFrenchWords() {
        val input = listOf(
            "Poivre h",
            "beurre H",
            "40 g de cerneaux de noix i",
            "1/2 céleri-rave",
            "2 oignons"
        )

        val beautified = Ocr5BeautifierIngredient.beautifyIngredients(input)

        assertWithMessage("'Poivre h' doit purger la lettre parasite 'h'")
            .that(beautified)
            .contains("Poivre")

        assertWithMessage("'beurre H' doit purger la lettre parasite 'H'")
            .that(beautified)
            .contains("Beurre")

        assertWithMessage("'40 g de cerneaux de noix i' doit purger la lettre parasite 'i'")
            .that(beautified)
            .contains("40 g de cerneaux de noix")

        assertWithMessage("'1/2 céleri-rave' doit préserver la terminaison 'rave'")
            .that(beautified)
            .contains("1/2 céleri-rave")

        assertWithMessage("'2 oignons' doit préserver le mot 'oignons'")
            .that(beautified)
            .contains("2 oignons")
    }
}
