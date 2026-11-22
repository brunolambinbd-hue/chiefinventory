package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la règle d'accord du pluriel français (Q > 1.0) dans [Ocr5BeautifierIngredient].
 */
class OcrIngredientPluralizationTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplyFrenchPluralIfQuantityGreaterThanOne_AccordPlurielCorrectement() {
        val line1 = Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("4 petites tomate", 4.0, vocabulary)
        assertWithMessage("Pour la quantité 4.0, 'tomate' doit prendre un 's'")
            .that(line1)
            .isEqualTo("4 petites tomates")

        val line2 = Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 gousse d'ail", 2.0, vocabulary)
        assertWithMessage("Pour la quantité 2.0, 'gousse' doit prendre un 's'")
            .that(line2)
            .isEqualTo("2 gousses d'ail")

        val line3 = Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 poireau", 2.0, vocabulary)
        assertWithMessage("Pour la quantité 2.0, 'poireau' (terminaison -eau) doit prendre un 'x'")
            .that(line3)
            .isEqualTo("2 poireaux")

        val line4 = Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("1 beau chicon", 1.0, vocabulary)
        assertWithMessage("Pour la quantité 1.0, 'chicon' doit rester au singulier")
            .that(line4)
            .isEqualTo("1 beau chicon")

        val line5 = Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("3 oignons", 3.0, vocabulary)
        assertWithMessage("Un mot se terminant déjà par 's' doit rester inchangé")
            .that(line5)
            .isEqualTo("3 oignons")
    }
}
