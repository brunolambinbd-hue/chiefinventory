package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr5BeautifierSourceNoiseHelper].
 */
class Ocr5BeautifierSourceNoiseHelperTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr5BeautifierSourceNoiseHelper doit générer une représentation textuelle valide")
            .that(Ocr5BeautifierSourceNoiseHelper.toString())
            .contains("Ocr5BeautifierSourceNoiseHelper")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanIsolatedAlphaNoiseFromSource_EdgeCases() {
        assertWithMessage("Le préfixe 'A ' devant 'Maxi GB' doit être purgé")
            .that(Ocr5BeautifierSourceNoiseHelper.cleanIsolatedAlphaNoiseFromSource("A Maxi GB"))
            .isEqualTo("Maxi GB")

        assertWithMessage("Le suffixe ' O' après 'Maxi GB' doit être purgé")
            .that(Ocr5BeautifierSourceNoiseHelper.cleanIsolatedAlphaNoiseFromSource("Maxi GB O"))
            .isEqualTo("Maxi GB")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsIsolatedAlphaNoise_AllBranches() {
        assertWithMessage("Une chaîne vide ou blanche doit être qualifiée de bruit d'alpha isolés")
            .that(Ocr5BeautifierSourceNoiseHelper.isIsolatedAlphaNoise(""))
            .isTrue()

        assertWithMessage("Une chaîne d'espaces doit être qualifiée de bruit d'alpha isolés")
            .that(Ocr5BeautifierSourceNoiseHelper.isIsolatedAlphaNoise("   "))
            .isTrue()

        assertWithMessage("Une lettre seule 'A.' doit être qualifiée de bruit d'alpha isolés")
            .that(Ocr5BeautifierSourceNoiseHelper.isIsolatedAlphaNoise("A."))
            .isTrue()

        assertWithMessage("Une paire de lettres isolées 'A. O' doit être qualifiée de bruit d'alpha isolés")
            .that(Ocr5BeautifierSourceNoiseHelper.isIsolatedAlphaNoise("A. O"))
            .isTrue()

        assertWithMessage("Une source valide 'Maxi GB' ne doit pas être qualifiée de bruit")
            .that(Ocr5BeautifierSourceNoiseHelper.isIsolatedAlphaNoise("Maxi GB"))
            .isFalse()
    }
}
