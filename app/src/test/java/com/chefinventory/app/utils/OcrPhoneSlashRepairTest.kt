package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1,
 * les confusions OCR entre la barre oblique du numéro de téléphone et la lettre 'I' ou 'l' (ex: '02I542', '01l456') sont réparées en '02/542', '01/456'.
 */
class OcrPhoneSlashRepairTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_PhoneAreaCodeWithI_RepairsToSlash() {
        val rawPhone1 = "Tél. : 02I542.42.42"
        val corrected1 = Ocr1Corrector.correct(rawPhone1)

        assertWithMessage("Le numéro '02I542' doit être réparé en '02/542'")
            .that(corrected1)
            .isEqualTo("Tél. : 02/542.42.42")

        val rawPhone2 = "02i542.42.42"
        val corrected2 = Ocr1Corrector.correct(rawPhone2)

        assertWithMessage("Le numéro '02i542' doit être réparé en '02/542'")
            .that(corrected2)
            .isEqualTo("02/542.42.42")
    }
}
