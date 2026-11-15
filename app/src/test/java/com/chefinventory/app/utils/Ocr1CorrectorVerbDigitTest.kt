package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de niveau A (Normalisateur Étape 1) vérifiant la séparation automatique
 * des verbes/mots collés à un chiffre (ex: 'Ajoutez2' -> 'Ajoutez 2', 'Versez1' -> 'Versez 1').
 */
class Ocr1CorrectorVerbDigitTest {

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
    fun testNormalize_VerbsConcatenatedWithDigits_InsertsSpaceCorrectly() {
        val input1 = "Ajoutez2 c. à soupe de sucre"
        val input2 = "Versez1/2 verre d'eau"
        val input3 = "Laissez10 minutes reposer"
        val input4 = "Comptez15 mn de cuisson"

        assertWithMessage("'Ajoutez2' doit être corrigé en 'Ajoutez 2'")
            .that(Ocr1Corrector.correct(input1))
            .isEqualTo("Ajoutez 2 c. à soupe de sucre")

        assertWithMessage("'Versez1/2' doit être corrigé en 'Versez 1/2'")
            .that(Ocr1Corrector.correct(input2))
            .isEqualTo("Versez 1/2 verre d'eau")

        assertWithMessage("'Laissez10' doit être corrigé en 'Laissez 10'")
            .that(Ocr1Corrector.correct(input3))
            .isEqualTo("Laissez 10 minutes reposer")

        assertWithMessage("'Comptez15' doit être corrigé en 'Comptez 15'")
            .that(Ocr1Corrector.correct(input4))
            .isEqualTo("Comptez 15 mn de cuisson")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCorrect_BulletH_TranscribesToHalf() {
        val input1 = "•h céleri-rave"
        val input2 = ".h citron"
        val input3 = "• h oignon"

        assertWithMessage("'•h céleri-rave' doit être transcrit en '1/2 céleri-rave'")
            .that(Ocr1Corrector.correct(input1))
            .isEqualTo("1/2 céleri-rave")

        assertWithMessage("'.h citron' doit être transcrit en '1/2 citron'")
            .that(Ocr1Corrector.correct(input2))
            .isEqualTo("1/2 citron")

        assertWithMessage("'• h oignon' doit être transcrit en '1/2 oignon'")
            .that(Ocr1Corrector.correct(input3))
            .isEqualTo("1/2 oignon")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCorrect_GcedillaSymbol_PurgesLeadingNoise() {
        val input1 = "ģ- Pour une version encore plus gourmande"
        val input2 = "ģ Pour une variante"
        val input3 = "¶ Pour la décoration"

        assertWithMessage("'ģ- Pour...' doit purger le symbole 'ģ-'")
            .that(Ocr1Corrector.correct(input1))
            .isEqualTo("Pour une version encore plus gourmande")

        assertWithMessage("'ģ Pour...' doit purger le symbole 'ģ'")
            .that(Ocr1Corrector.correct(input2))
            .isEqualTo("Pour une variante")

        assertWithMessage("'¶ Pour...' doit purger le symbole '¶'")
            .that(Ocr1Corrector.correct(input3))
            .isEqualTo("Pour la décoration")
    }
}
