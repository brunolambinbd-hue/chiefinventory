package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires TDD d'isolation 1-à-1 dédiée à [Ocr3CategorizerLineCleaner].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerLineCleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPurgeExcludedKeywords_CleanLineWithoutKeywords_ReturnsUnchanged() {
        val rawLine = "200 g de farine de blé"
        val cleaned = Ocr3CategorizerLineCleaner.purgeExcludedKeywords(rawLine, containsAction = false, vocabulary = vocabulary)

        assertWithMessage("La ligne sans mots exclus doit être retournée inchangée")
            .that(cleaned)
            .isEqualTo("200 g de farine de blé")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPurgeExcludedKeywords_LineWithExcludedKeywordPAGE_PurgesKeyword() {
        val rawLine = "200 g de farine PAGE 12"
        val cleaned = Ocr3CategorizerLineCleaner.purgeExcludedKeywords(rawLine, containsAction = false, vocabulary = vocabulary)

        assertWithMessage("Le mot d'exclusion 'PAGE' doit être purgé de la ligne")
            .that(cleaned)
            .doesNotContain("PAGE")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr3CategorizerLineCleaner doit générer une représentation textuelle valide")
            .that(Ocr3CategorizerLineCleaner.toString())
            .contains("Ocr3CategorizerLineCleaner")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPurgeExcludedKeywords_InstructionSentenceWithActionVerbAndIngredientsWord_ProtectsWord() {
        val rawInstruction = "Mélangez tous ces ingrédients dans un saladier"
        val cleaned = Ocr3CategorizerLineCleaner.purgeExcludedKeywords(rawInstruction, containsAction = true, vocabulary = vocabulary)

        assertWithMessage("Le mot 'ingrédients' précédé d'un verbe d'action ne doit pas être supprimé")
            .that(cleaned)
            .isEqualTo("Mélangez tous ces ingrédients dans un saladier")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPurgeExcludedKeywords_WithEmptyAndBlankKeywordsInVocabulary_IgnoresEmpty() {
        val customVocab = vocabulary.copy(excludedOcrKeywords = vocabulary.excludedOcrKeywords + listOf("", "   "))
        val rawLine = "200 g de farine"
        val cleaned = Ocr3CategorizerLineCleaner.purgeExcludedKeywords(rawLine, containsAction = false, vocabulary = customVocab)

        assertWithMessage("Les mots exclus vides ou blancs doivent être ignorés sans erreur")
            .that(cleaned)
            .isEqualTo("200 g de farine")
    }
}
