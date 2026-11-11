package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires dédiés à l'intégrité du chargement et du contenu des fichiers XML de vocabulaire OCR.
 */
class OcrVocabularyXmlTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `loadFromProjectResources should load non-empty diacritic repairs list`() {
        assertWithMessage("La liste des réparations diacritiques chargées depuis le XML ne doit pas être vide")
            .that(vocabulary.ocrDiacriticRepairs)
            .isNotEmpty()
    }

    @Test
    fun `defaultDiacriticRepairs should convert accented characters correctly`() {
        assertWithMessage("Le diacritique corrompu 'ế' doit être corrigé en 'é'")
            .that(Ocr1Corrector.correct("ế"))
            .isEqualTo("é")

        assertWithMessage("Le diacritique corrompu 'ề' doit être corrigé en 'è'")
            .that(Ocr1Corrector.correct("ề"))
            .isEqualTo("è")

        assertWithMessage("Le diacritique corrompu 'ấ' doit être corrigé en 'â'")
            .that(Ocr1Corrector.correct("ấ"))
            .isEqualTo("â")

        assertWithMessage("Le diacritique corrompu 'ầ' doit être corrigé en 'à'")
            .that(Ocr1Corrector.correct("ầ"))
            .isEqualTo("à")

        assertWithMessage("Le diacritique corrompu 'ặ' doit être corrigé en 'a'")
            .that(Ocr1Corrector.correct("ặ"))
            .isEqualTo("a")
    }

    @Test
    fun `loadFromProjectResources should load non-empty orphan noun number and stop words`() {
        assertWithMessage("Les mots de nombres pour noms orphelins doivent être chargés depuis le XML")
            .that(vocabulary.orphanNounNumberWords)
            .isNotEmpty()

        assertWithMessage("Les mots-morts pour noms orphelins doivent être chargés depuis le XML")
            .that(vocabulary.orphanNounStopWords)
            .isNotEmpty()
    }

    @Test
    fun `loadFromProjectResources should load non-empty protected headers`() {
        assertWithMessage("Les en-têtes d'ingrédients protégés de l'Étape 1 doivent être chargés")
            .that(vocabulary.ocr1ProtectedIngredientsHeaders)
            .isNotEmpty()

        assertWithMessage("Les en-têtes d'instructions protégés de l'Étape 1 doivent être chargés")
            .that(vocabulary.ocr1ProtectedInstructionsHeaders)
            .isNotEmpty()
    }

    @Test
    fun `loadFromProjectResources should load non-empty culinary measurement units`() {
        assertWithMessage("Les unités de mesure culinaires doivent être chargées depuis le XML")
            .that(vocabulary.culinaryMeasurementUnits)
            .isNotEmpty()
    }
}
