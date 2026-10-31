package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'extraction de [Ocr3NarrativeCategorizer].
 */
class Ocr3NarrativeCategorizerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_EmptyInput_ReturnsFalse() {
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(emptyList(), vocabulary)).isFalse()
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(listOf("   "), vocabulary)).isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_InstructionHeaderWithoutQuantifiedIngredients() {
        val lines = listOf(
            "Gâteau Express",
            "instructions :",
            "Mélanger la farine avec le sucre.",
            "Enfourner pendant 20 minutes."
        )

        val isNarrative = Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(lines, vocabulary)
        assertThat(isNarrative).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_ExhaustiveHeadersAndDisqualification() {
        // Disqualified because it contains an explicit ingredient header OR instruction header + ingredient
        val linesWithIngrHeader1 = listOf("composition :", "200 g de farine", "Faire cuire au four")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader1, vocabulary)).isFalse()

        val linesWithIngrHeader2 = listOf("ingredients :", "200 g de farine", "Faire cuire au four")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader2, vocabulary)).isFalse()

        val linesWithIngrHeader3 = listOf("ngrédients :", "200 g de farine", "Faire cuire au four")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader3, vocabulary)).isFalse()

        val linesWithBothHeaders = listOf("réalisation :", "200 g de farine", "Faire cuire")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBothHeaders, vocabulary)).isFalse()

        // Disqualified because it has a bottom ingredient list ("pour" on line 2, "4 personnes" on line 3)
        val linesWithBottomList1 = listOf("Préparation :", "Faire cuire.", "pour 4 personnes", "200 g de farine")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBottomList1, vocabulary)).isFalse()

        val linesWithBottomList2 = listOf("Préparation :", "Faire cuire.", "pour", "4 personnes", "200 g de farine")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBottomList2, vocabulary)).isFalse()

        // Test narrative headers and step number formats
        val narrativeHeaders = listOf(
            listOf("Gâteau Express", "1. Mélanger la farine avec du sucre."),
            listOf("Tarte Express", "• Faire cuire la pâte."),
            listOf("Sablé Express", "- Incorporez le beurre.")
        )
        narrativeHeaders.forEach { narrativeLines ->
            assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(narrativeLines, vocabulary)).isTrue()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_StandaloneLinesAndDuplicateSources() {
        val linesWithStandaloneIngr = listOf("200 g de farine", "Mélanger avec du beurre")
        assertThat(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithStandaloneIngr, vocabulary)).isFalse()

        val narrativeWithDuplicates = listOf(
            "Soupe de Légumes",
            "Mélanger les légumes.",
            "Hôtel Hilton, Place Rogier",
            "Hôtel Hilton, Place Rogier"
        )
        val result = Ocr3NarrativeCategorizer.categorizeNarrativeRecipe(narrativeWithDuplicates, vocabulary)
        assertThat(result.detectedSourceList).containsExactly("Hôtel Hilton, Place Rogier")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeNarrativeRecipe_SourceLineExtraction() {
        val lines = listOf(
            "Soupe de Légumes Express",
            "Faites revenir 1 oignon haché et 2 carottes.",
            "Hôtel Hilton, Place Rogier" // Source line
        )

        val result = Ocr3NarrativeCategorizer.categorizeNarrativeRecipe(lines, vocabulary)
        assertThat(result.detectedSourceList).contains("Hôtel Hilton, Place Rogier")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_GateauAuChocolatExpress() {
        val ocrText = """
            Gâteau au Chocolat Express
            Mélangez 200 g de chocolat noir fondu avec 100 g de beurre et 3 œufs. 
            Ajoutez 80 g de farine et 100 g de sucre.
            Faites cuire au four pendant 20 minutes à 180°C.
        """.trimIndent()

        val isNarrative = Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(ocrText.lines(), vocabulary)
        assertThat(isNarrative).isTrue()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        assertThat(result.title).contains("Gâteau")
        assertThat(result.instructions).contains("cuire au four")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorizeNarrativeRecipe_SoupeGlaceeAuConcombre() {
        val rawOcr = """
            Soupe glacée
            au concombre
            1 Versez 1 concombre en
            Préparation: 10 mn morceaux, 250 g de yaourt au
            Bon marché
            lait de brebis bien froid, 2 c. à soupe
            de menthe fraiche hachée, 1
            gousse d'ail pelée, sel et poivre
            dans le bol d'un robot ménager.
            2 Mixez en velouté lisse.
            3 Servez dans des bols glacés et
            garnissez de feuilles de menthe.
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcr, vocabulary)

        assertThat(result.ingredients).contains("concombre")
        assertThat(result.ingredients).contains("yaourt")
        assertThat(result.ingredients).contains("menthe")
        assertThat(result.ingredients).contains("ail")
        assertThat(result.ingredients).contains("Sel et poivre")
        assertThat(result.instructions).contains("Versez 1 concombre")
    }
}
