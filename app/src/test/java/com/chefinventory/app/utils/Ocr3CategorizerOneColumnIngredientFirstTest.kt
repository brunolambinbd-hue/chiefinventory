package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerOneColumnIngredientFirst] (Lecteur 1 : 1 colonne, ingrédients d'abord).
 */
class Ocr3CategorizerOneColumnIngredientFirstTest {

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
    fun testOneColumnIngredientFirst_ExtractsIngredientsThenInstructions() {
        val lines = listOf(
            "SALADE DE TOMATES",
            "INGRÉDIENTS",
            "• 4 tomates",
            "• 2 c. à soupe d'huile",
            "INSTRUCTIONS",
            "• Coupez les tomates en dés.",
            "• Mélangez avec l'huile."
        )

        val sections = Ocr3CategorizerOneColumnIngredientFirst.categorize(lines, vocabulary)

        assertThat(sections.rawIngredientsList).containsAtLeast("4 tomates", "2 c. à soupe d'huile")
        assertThat(sections.rawInstructionsList).containsExactly("Coupez les tomates en dés.", "Mélangez avec l'huile.").inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAllBranchesAndLoggingCoverage() {
        val customVocab = vocabulary.copy(
            logOcr3CategorizerNarrativeDelegation = "Délégation recette narrative",
            logOcr3CategorizerStart = "Début catégorisation 1D",
            logOcr3CategorizerEnd = "Fin catégorisation 1D"
        )

        val narrativeLines = listOf(
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile d'olive pendant 10 min.",
            "Servez chaud."
        )
        val narrativeSections = Ocr3CategorizerOneColumnIngredientFirst.categorize(narrativeLines, customVocab)
        assertThat(narrativeSections.rawInstructionsList).hasSize(2)

        // Blank logging branches test
        val blankLoggingVocab = vocabulary.copy(
            logOcr3CategorizerNarrativeDelegation = "",
            logOcr3CategorizerStart = "",
            logOcr3CategorizerEnd = ""
        )
        val narrativeBlankSections = Ocr3CategorizerOneColumnIngredientFirst.categorize(narrativeLines, blankLoggingVocab)
        assertThat(narrativeBlankSections.rawInstructionsList).hasSize(2)

        val classicLines = listOf(
            "INGRÉDIENTS",
            "• 4 tomates",
            "INSTRUCTIONS",
            "• Coupez les tomates."
        )
        val classicSections = Ocr3CategorizerOneColumnIngredientFirst.categorize(classicLines, customVocab)
        assertThat(classicSections.rawIngredientsList).contains("4 tomates")

        val classicBlankSections = Ocr3CategorizerOneColumnIngredientFirst.categorize(classicLines, blankLoggingVocab)
        assertThat(classicBlankSections.rawIngredientsList).contains("4 tomates")

        // Single line recipe -> nextLine else branch (i + 1 < lines.size is false)
        val singleLineDirect = Ocr3CategorizerOneColumnIngredientFirst.categorizeDirect(listOf("• 4 tomates"), customVocab)
        assertThat(singleLineDirect.rawIngredientsList).contains("4 tomates")

        val keywordsWithSupplemental = Ocr3CategorizerOneColumnIngredientFirst.extractKeywordsFromText("tomates", "bio d'Espagne")
        assertThat(keywordsWithSupplemental).containsAtLeast("tomates", "espagne")

        val keywordsWithoutSupplemental = Ocr3CategorizerOneColumnIngredientFirst.extractKeywordsFromText("tomates", null)
        assertThat(keywordsWithoutSupplemental).contains("tomates")
    }
}
