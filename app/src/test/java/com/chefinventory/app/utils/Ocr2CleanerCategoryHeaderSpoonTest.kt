package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la non-détection des en-têtes de catégorie sur les unités de cuillères ('c. à dessert') dans [Ocr2Cleaner].
 */
class Ocr2CleanerCategoryHeaderSpoonTest {

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
    fun testCleaner_SpoonUnitLine_DoesNotTriggerCategoryHeaderAndPreservesLine() {
        val lines = listOf(
            "100 g de chair de crabe",
            "1 c. à dessert de rhum",
            "poivre de Cayenne"
        )

        val result = Ocr2Cleaner.cleanWithVocabulary(lines, vocabulary)

        assertWithMessage("La ligne '1 c. à dessert de rhum' ne doit PAS déclencher la catégorie 'Dessert'")
            .that(result.earlyDetectedCategory)
            .isNull()

        assertWithMessage("La ligne '1 c. à dessert de rhum' ne doit pas être purgée du texte nettoyé")
            .that(result.cleanedLines)
            .contains("1 c. à dessert de rhum")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleaner_StandaloneCategoryHeader_TriggersCategoryHeaderAndPurgesLine() {
        val lines = listOf(
            "DESSERTS ET GÂTEAUX",
            "Tarte Tatin"
        )

        val result = Ocr2Cleaner.cleanWithVocabulary(lines, vocabulary)

        assertWithMessage("L'en-tête de catégorie 'DESSERTS ET GÂTEAUX' doit capturer la catégorie 'Dessert'")
            .that(result.earlyDetectedCategory)
            .isEqualTo("Dessert")

        assertWithMessage("L'en-tête de catégorie 'DESSERTS ET GÂTEAUX' doit être purgé du texte nettoyé")
            .that(result.cleanedLines)
            .containsExactly("Tarte Tatin")
    }
}
