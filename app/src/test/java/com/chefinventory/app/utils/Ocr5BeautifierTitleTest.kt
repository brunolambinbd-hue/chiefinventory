package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau Étape 5 ([Ocr5BeautifierTitle]) vérifiant l'harmonisation
 * et la majusculation initiale en Sentence case des titres de recettes.
 */
class Ocr5BeautifierTitleTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyTitle_VariousTitleFormats_HarmonizesToSentenceCase() {
        val title1 = "SOUPE VERTE aux LÉGUMES D'HIVER ET AU ROQUEFORT"
        val title2 = "TARTE AUX POMMES ET AU MIEL"
        val title3 = "CANAPÉS GRECS AU PARMESAN"

        assertWithMessage("'SOUPE VERTE...' doit être harmonisé en Sentence case")
            .that(Ocr5BeautifierTitle.beautifyTitle(title1))
            .isEqualTo("Soupe verte aux légumes d'hiver et au roquefort")

        assertWithMessage("'TARTE AUX POMMES...' doit être harmonisé en Sentence case")
            .that(Ocr5BeautifierTitle.beautifyTitle(title2))
            .isEqualTo("Tarte aux pommes et au miel")

        assertWithMessage("'CANAPÉS GRECS...' doit être harmonisé en Sentence case")
            .that(Ocr5BeautifierTitle.beautifyTitle(title3))
            .isEqualTo("Canapés grecs au parmesan")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyTitle_NullEmptyAndPunctuationOnly_ReturnsOriginal() {
        assertWithMessage("Un titre null doit retourner null")
            .that(Ocr5BeautifierTitle.beautifyTitle(null))
            .isNull()

        assertWithMessage("Un titre vide doit retourner la chaîne vide")
            .that(Ocr5BeautifierTitle.beautifyTitle(""))
            .isEmpty()

        assertWithMessage("Un titre d'espaces doit retourner les espaces")
            .that(Ocr5BeautifierTitle.beautifyTitle("   "))
            .isEqualTo("   ")

        assertWithMessage("Un titre de ponctuation seule doit retourner la ponctuation")
            .that(Ocr5BeautifierTitle.beautifyTitle(":"))
            .isEqualTo(":")
    }
}
