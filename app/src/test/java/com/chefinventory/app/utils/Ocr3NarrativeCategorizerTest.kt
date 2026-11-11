package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Une liste de lignes vide ne doit pas être qualifiée de recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(emptyList(), vocabulary))
            .isFalse()

        assertWithMessage("Une liste contenant uniquement un espace ne doit pas être qualifiée de recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(listOf("   "), vocabulary))
            .isFalse()
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
        assertWithMessage("Une fiche avec en-tête d'instructions sans ingrédients quantifiés doit être qualifiée de narrative")
            .that(isNarrative)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_ExhaustiveHeadersAndDisqualification() {
        // Disqualified because it contains an explicit ingredient header OR instruction header + ingredient
        val linesWithIngrHeader1 = listOf("composition :", "200 g de farine", "Faire cuire au four")
        assertWithMessage("L'en-tête explicite 'composition :' doit disqualifier la recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader1, vocabulary))
            .isFalse()

        val linesWithIngrHeader2 = listOf("ingredients :", "200 g de farine", "Faire cuire au four")
        assertWithMessage("L'en-tête explicite 'ingredients :' doit disqualifier la recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader2, vocabulary))
            .isFalse()

        val linesWithIngrHeader3 = listOf("ngrédients :", "200 g de farine", "Faire cuire au four")
        assertWithMessage("L'en-tête 'ngrédients :' doit disqualifier la recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithIngrHeader3, vocabulary))
            .isFalse()

        val linesWithBothHeaders = listOf("réalisation :", "200 g de farine", "Faire cuire")
        assertWithMessage("La présence d'un ingrédient quantifié avec 'réalisation :' doit disqualifier la recette narrative")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBothHeaders, vocabulary))
            .isFalse()

        // Disqualified because it has a bottom ingredient list ("pour" on line 2, "4 personnes" on line 3)
        val linesWithBottomList1 = listOf("Préparation :", "Faire cuire.", "pour 4 personnes", "200 g de farine")
        assertWithMessage("La présence d'un bloc d'ingrédients du bas 'pour 4 personnes' doit disqualifier le mode narratif")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBottomList1, vocabulary))
            .isFalse()

        val linesWithBottomList2 = listOf("Préparation :", "Faire cuire.", "pour", "4 personnes", "200 g de farine")
        assertWithMessage("La présence d'un bloc d'ingrédients du bas sur 2 lignes doit disqualifier le mode narratif")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithBottomList2, vocabulary))
            .isFalse()

        // Test narrative headers and step number formats
        val narrativeHeaders = listOf(
            listOf("Gâteau Express", "1. Mélanger la farine avec du sucre."),
            listOf("Tarte Express", "• Faire cuire la pâte."),
            listOf("Sablé Express", "- Incorporez le beurre.")
        )
        narrativeHeaders.forEach { narrativeLines ->
            assertWithMessage("La variante d'instructions narratives pour '${narrativeLines[0]}' doit être qualifiée de narrative")
                .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(narrativeLines, vocabulary))
                .isTrue()
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsNarrativeInstructionOnlyRecipe_StandaloneLinesAndDuplicateSources() {
        val linesWithStandaloneIngr = listOf("200 g de farine", "Mélanger avec du beurre")
        assertWithMessage("Une ligne d'ingrédient autonome isolée '200 g de farine' doit disqualifier le mode narratif")
            .that(Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(linesWithStandaloneIngr, vocabulary))
            .isFalse()

        val narrativeWithDuplicates = listOf(
            "Soupe de Légumes",
            "Mélanger les légumes.",
            "Hôtel Hilton, Place Rogier",
            "Hôtel Hilton, Place Rogier"
        )
        val result = Ocr3NarrativeCategorizer.categorizeNarrativeRecipe(narrativeWithDuplicates, vocabulary)
        assertWithMessage("La liste des sources de la recette narrative doit dé-doublonner 'Hôtel Hilton, Place Rogier'")
            .that(result.detectedSourceList)
            .containsExactly("Hôtel Hilton, Place Rogier")
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
        assertWithMessage("La source d'établissement 'Hôtel Hilton, Place Rogier' doit être extraite dans detectedSourceList")
            .that(result.detectedSourceList)
            .contains("Hôtel Hilton, Place Rogier")
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
        assertWithMessage("Le texte du Gâteau au chocolat express doit être qualifié de recette narrative")
            .that(isNarrative)
            .isTrue()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        assertWithMessage("Le titre extrait du Gâteau au chocolat express doit contenir 'Gâteau'")
            .that(result.title)
            .contains("Gâteau")

        assertWithMessage("Les instructions doivent contenir 'cuire au four'")
            .that(result.instructions)
            .contains("cuire au four")
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

        assertWithMessage("L'ingrédient implicite 'concombre' doit être extrait")
            .that(result.ingredients)
            .contains("concombre")

        assertWithMessage("L'ingrédient implicite 'yaourt' doit être extrait")
            .that(result.ingredients)
            .contains("yaourt")

        assertWithMessage("L'ingrédient implicite 'menthe' doit être extrait")
            .that(result.ingredients)
            .contains("menthe")

        assertWithMessage("L'ingrédient implicite 'ail' doit être extrait")
            .that(result.ingredients)
            .contains("ail")

        assertWithMessage("L'assaisonnement implicite 'Sel et poivre' doit être extrait")
            .that(result.ingredients)
            .contains("Sel et poivre")

        assertWithMessage("Les instructions doivent contenir 'Versez 1 concombre'")
            .that(result.instructions)
            .contains("Versez 1 concombre")
    }
}
