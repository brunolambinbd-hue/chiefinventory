package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires isolés pour les réparations d'accents et diacritiques parasites Unicode (`ocr_step1_diacritic_repairs.xml`).
 */
class OcrDiacriticRepairsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `defaultDiacriticRepairs should convert accented characters correctly`() {
        val diacritics = vocabulary.ocrDiacriticRepairs
        assertWithMessage("Le diacritique corrompu 'ế' doit être réparé en 'é'")
            .that(Ocr1Corrector.correct("ế", diacriticRepairs = diacritics))
            .isEqualTo("é")

        assertWithMessage("Le diacritique corrompu 'ề' doit être réparé en 'è'")
            .that(Ocr1Corrector.correct("ề", diacriticRepairs = diacritics))
            .isEqualTo("è")

        assertWithMessage("Le diacritique corrompu 'ấ' doit être réparé en 'â'")
            .that(Ocr1Corrector.correct("ấ", diacriticRepairs = diacritics))
            .isEqualTo("â")

        assertWithMessage("Le diacritique corrompu 'ầ' doit être réparé en 'à'")
            .that(Ocr1Corrector.correct("ầ", diacriticRepairs = diacritics))
            .isEqualTo("à")

        assertWithMessage("Le diacritique corrompu 'ặ' doit être réparé en 'a'")
            .that(Ocr1Corrector.correct("ặ", diacriticRepairs = diacritics))
            .isEqualTo("a")

        assertWithMessage("Le diacritique corrompu 'Ė' doit être réparé en 'É'")
            .that(Ocr1Corrector.correct("Ė", diacriticRepairs = diacritics))
            .isEqualTo("É")

        assertWithMessage("Le diacritique corrompu 'ė' doit être réparé en 'é'")
            .that(Ocr1Corrector.correct("ė", diacriticRepairs = diacritics))
            .isEqualTo("é")

        assertWithMessage("Le diacritique corrompu 'ố' doit être réparé en 'ô'")
            .that(Ocr1Corrector.correct("ố", diacriticRepairs = diacritics))
            .isEqualTo("ô")

        assertWithMessage("Le diacritique corrompu 'ỉ' doit être réparé en 'î'")
            .that(Ocr1Corrector.correct("ỉ", diacriticRepairs = diacritics))
            .isEqualTo("î")
    }

    @Test
    fun `applyDiacriticRepairs should fallback gracefully on empty list or invalid format`() {
        assertWithMessage("Une liste de diacritiques vide doit retourner le texte d'origine")
            .that(Ocr1Corrector.applyDiacriticRepairs("test", emptyList()))
            .isEqualTo("test")

        val withInvalidDiacritic = Ocr1Corrector.correct("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertWithMessage("Une règle de diacritique invalide sans pipe '|' doit être ignorée")
            .that(withInvalidDiacritic)
            .isEqualTo("ế")
    }

    @Test
    fun testNormalize_EmptyHeaderListsAndHeaderMatchNoChange() {
        val emptyVocab = OcrVocabulary()
        val result = Ocr1Corrector.correct("ingrédients", diacriticRepairs = emptyVocab.ocrDiacriticRepairs)
        assertWithMessage("Un en-tête canonique 'ingrédients' doit être conservé tel quel")
            .that(result)
            .isEqualTo("ingrédients")
    }
}
