package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la beautification des titres par Levenshtein à l'Étape 5 ([Ocr5BeautifierTitle]).
 */
class Ocr5BeautifierTitleLevenshteinTest {

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
    fun testBeautifyTitle_FixesTypoedTitleWordsWithLevenshteinAtStep5() {
        val title1 = Ocr5BeautifierTitle.beautifyTitle("Haricots fprincesses au parmesan", vocabulary)
        assertWithMessage("La typo 'fprincesses' dans le titre doit être corrigée en 'princesses'")
            .that(title1)
            .isEqualTo("Haricots princesses au parmesan")

        val title2 = Ocr5BeautifierTitle.beautifyTitle("PAELLA DE VALENCIA", vocabulary)
        assertWithMessage("Le mot non présent dans le dictionnaire 'PAELLA' doit rester inchangé en Sentence Case")
            .that(title2)
            .isEqualTo("Paella de valencia")

        val title3 = Ocr5BeautifierTitle.beautifyTitle("CANAPÉ3 GRECS", vocabulary)
        assertWithMessage("La typo 'CANAPÉ3' dans le titre doit être corrigée en 'Canapés'")
            .that(title3)
            .isEqualTo("Canapés grecs")
    }
}
