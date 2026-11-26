package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1 (Ocr1Corrector),
 * la confusion OCR entre le chiffre '1' et la lettre 'l' ou 'I' dans un numéro de rue d'adresse postale (ex: 'avenue Louise 7l')
 * est réparée en 'Avenue Louise 71', tout en préservant intactes les quantités culinaires (ex: '7 l d'eau').
 */
class OcrAddressNumberRepairTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCorrect_AddressStreetNumberWithL_RepairsDigit1AndPreservesCulinaryUnits() {
        val rawAddress1 = "avenue Louise 7l"
        val corrected1 = Ocr1Corrector.correct(rawAddress1)

        assertWithMessage("L'adresse 'avenue Louise 7l' doit être réparée en 'avenue Louise 71'")
            .that(corrected1)
            .isEqualTo("avenue Louise 71")

        val rawAddress2 = "rue Royale 12l"
        val corrected2 = Ocr1Corrector.correct(rawAddress2)

        assertWithMessage("L'adresse 'rue Royale 12l' doit être réparée en 'rue Royale 121'")
            .that(corrected2)
            .isEqualTo("rue Royale 121")

        val rawCulinary = "7 l d'eau"
        val correctedCulinary = Ocr1Corrector.correct(rawCulinary)

        assertWithMessage("La quantité culinaire '7 l d'eau' doit rester intacte sans modification")
            .that(correctedCulinary)
            .isEqualTo("7 l d'eau")
    }
}
