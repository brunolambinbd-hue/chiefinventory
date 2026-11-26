package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant qu'en Étape 1 (Ocr1Corrector),
 * le dictionnaire de réparations sensibles à la casse stricte (ocr_case_sensitive_spelling_repairs.xml)
 * corrige les majuscules parasites au milieu des mots (ex: 'RecouVrez' -> 'Recouvrez').
 */
class OcrCaseSensitiveSpellingRepairsTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        OcrRegexesDynamicHelpers.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplySpellingRepairs_RepairsInternalCapitalLetter() {
        val repairs = listOf("RecouVrez|Recouvrez")

        // 1. Cas Direct : Mot présent dans la liste -> Corrigé
        val rawLine1 = "•RecouVrez les épinards avec"
        val corrected1 = Ocr1Corrector.applySpellingRepairs(rawLine1, repairs)
        assertWithMessage("Le mot 'RecouVrez' avec V majuscule doit être corrigé en 'Recouvrez'")
            .that(corrected1)
            .contains("Recouvrez")

        // 2. Cas Inverse : Mot avec majuscule absent de la liste -> Conservé inchangé
        val rawLine2 = "•Mélangez le Chocolat Noir"
        val corrected2 = Ocr1Corrector.applySpellingRepairs(rawLine2, repairs)
        assertWithMessage("Un mot avec majuscule absent de la liste doit rester inchangé avec sa majuscule")
            .that(corrected2)
            .isEqualTo("•Mélangez le Chocolat Noir")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplySpellingRepairs_WithFullXmlRepairsArray_RepairsInternalCapitalLetter() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        // 1. Cas Direct avec le dictionnaire XML complet : 'RecouVrez' -> 'Recouvrez'
        val rawLine1 = "•RecouVrez les épinards avec"
        val corrected1 = Ocr1Corrector.applyCaseSensitiveSpellingRepairs(rawLine1, vocabulary.ocrCaseSensitiveSpellingRepairs)
        assertWithMessage("Le mot 'RecouVrez' avec V majuscule doit être corrigé en 'Recouvrez' en utilisant le dictionnaire Case-Sensitive XML")
            .that(corrected1)
            .contains("Recouvrez")

        // 2. Cas Inverse avec le dictionnaire XML complet : Mot avec majuscule absent de l'XML -> Conservé inchangé
        val rawLine2 = "Mélangez le Chocolat Noir"
        val corrected2 = Ocr1Corrector.applyCaseSensitiveSpellingRepairs(rawLine2, vocabulary.ocrCaseSensitiveSpellingRepairs)
        assertWithMessage("Un mot avec majuscule absent du dictionnaire Case-Sensitive XML doit conserver sa majuscule intacte")
            .that(corrected2)
            .isEqualTo("Mélangez le Chocolat Noir")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCorrect_IntegrationPipelineWithCaseSensitiveXml_RepairsInternalCapitalLetter() {
        // 1. Cas Direct : 'RecouVrez' -> 'Recouvrez' via Ocr1Corrector.correct()
        val rawLine1 = "•RecouVrez les épinards avec"
        val corrected1 = Ocr1Corrector.correct(rawLine1)
        assertWithMessage("Le pipeline d'intégration Ocr1Corrector.correct() doit réparer 'RecouVrez' en 'Recouvrez' via le dictionnaire XML Case-Sensitive")
            .that(corrected1)
            .contains("Recouvrez")

        // 2. Cas Inverse : Mot avec majuscule absent du dictionnaire XML -> Conservé inchangé
        val rawLine2 = "Mélangez le Chocolat Noir"
        val corrected2 = Ocr1Corrector.correct(rawLine2)
        assertWithMessage("Un mot avec majuscule absent du dictionnaire XML doit conserver sa majuscule intacte dans Ocr1Corrector.correct()")
            .that(corrected2)
            .isEqualTo("Mélangez le Chocolat Noir")
    }
}
