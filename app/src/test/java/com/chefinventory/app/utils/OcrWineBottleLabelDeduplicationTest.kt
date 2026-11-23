package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 5 (Beautifier), les doublons corrompus
 * scannés graphiquement depuis l'étiquette d'une bouteille de vin ('CHATLAU', 'dla FOsSARDIERE') sont identifiés et purgés.
 */
class OcrWineBottleLabelDeduplicationTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyWines_CorruptedBottleLabelDuplicates_PurgedCleanly() {
        val rawWines = listOf(
            "Château de la Fossardière",
            "Muscadet de Sèvre-et-Maine sur Lie",
            "CHATLAU",
            "dla FOsSARDIERE"
        )

        val result = Ocr5BeautifierWine.beautifyWines(rawWines)

        assertWithMessage("Le vin officiel 'Château de la Fossardière' doit être conservé")
            .that(result)
            .contains("Château de la Fossardière")

        assertWithMessage("L'AOC 'Muscadet de Sèvre-et-Maine sur Lie' doit être conservée")
            .that(result)
            .contains("Muscadet de Sèvre-et-Maine sur Lie")

        assertWithMessage("Le doublon d'étiquette de bouteille 'CHATLAU' doit être purgé")
            .that(result)
            .doesNotContain("CHATLAU")

        assertWithMessage("Le doublon d'étiquette de bouteille 'dla FOsSARDIERE' doit être purgé")
            .that(result)
            .doesNotContain("dla FOsSARDIERE")
    }
}
