package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant l'extraction exacte du titre en majuscules sur 2 lignes
 * 'SOUPE AU CÉLERI-RAVE, NOIX ET MASCARPONE' et la séparation 2D des ingrédients et instructions.
 */
class OcrSoupeCeleriRaveTitleTest {

    private val rawOcrScanText = listOf(
        "SOUPE AU CÉLERI-RAVE,",
        "NOIX ET MASCARPONE",
        "O 25 MIN + 30 MIN DE CUISSON O FACILE O EEE",
        "A4PERS. O 233 KCALPOR.",
        "NUTRI-SCORE",
        "B",
        "• 1/2 céleri-rave",
        "• 2 oignons",
        "• 40g de cerneaux de noix",
        "• 2 gousses d'ail",
        "• 4 brins de thym",
        "• 2 c. à soupe de mascarpone",
        "• 20 g de beurre",
        "• 2 cubes de bouillon de légumes",
        "• Poivre",
        "• Sel",
        "INSTRUCTIONS",
        "Épluchez le céleri-rave et coupez-le en dés. Hachez grossièrement les oignons.",
        "Mettez le tout dans une casserole contenant le beurre fondu."
    ).joinToString("\n")

    private val lines2DScan = listOf(
        OcrTextLine("SOUPE AU CÉLERI-RAVE,", top = 18, left = 144, bottom = 171, right = 653),
        OcrTextLine("NOIX ET MASCARPONE", top = 97, left = 146, bottom = 234, right = 627),
        OcrTextLine("O 20 MIN + 30 MIN DE CUISSON (+ REFROIDISSEMENT) FACILE", top = 170, left = 149, bottom = 297, right = 714),
        OcrTextLine("4 PERS. 233 KCALPOR.", top = 251, left = 146, bottom = 336, right = 460),
        OcrTextLine("NUTRI-SCORE", top = 359, left = 160, bottom = 398, right = 271),
        OcrTextLine("•l poireau", top = 420, left = 144, bottom = 460, right = 340),
        OcrTextLine("•h céleri-rave", top = 467, left = 144, bottom = 530, right = 343),
        OcrTextLine("• 2 oignons", top = 515, left = 144, bottom = 577, right = 297),
        OcrTextLine("• 40g de cerneauX", top = 556, left = 141, bottom = 614, right = 392),
        OcrTextLine("de noix i", top = 603, left = 163, bottom = 653, right = 308),
        OcrTextLine("• 2 gousses d'ail S3", top = 637, left = 133, bottom = 709, right = 410),
        OcrTextLine("•4 brins de thym", top = 696, left = 129, bottom = 750, right = 364),
        OcrTextLine("• 100 g de roquefort", top = 720, left = 126, bottom = 750, right = 710),
        OcrTextLine("•2 càsde mascarpone", top = 381, left = 581, bottom = 468, right = 913),
        OcrTextLine("•20 g de beurre .", top = 437, left = 578, bottom = 518, right = 865),
        OcrTextLine("• 2 cubes de bouillon", top = 486, left = 589, bottom = 561, right = 879),
        OcrTextLine("de légumes", top = 543, left = 619, bottom = 608, right = 770),
        OcrTextLine(". Poivre h", top = 594, left = 577, bottom = 654, right = 749),
        OcrTextLine(".Sel", top = 662, left = 579, bottom = 697, right = 648),
        OcrTextLine("Épluchez le céleri-rave et coupez-le", top = 1756, left = 183, bottom = 1861, right = 1862)
    )

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSoupeCeleriRave_ExtractsFullTwoLineTitle_Cleanly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(rawOcrScanText, vocabulary)

        assertWithMessage("Le titre extrait doit contenir le mot de la première ligne 'soupe au céleri-rave'")
            .that(result.title)
            .ignoringCase()
            .contains("soupe au céleri-rave")

        assertWithMessage("Le titre extrait doit contenir les mots de la seconde ligne 'noix et mascarpone'")
            .that(result.title)
            .ignoringCase()
            .contains("noix et mascarpone")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSoupeCeleriRave_Headerless2D_ExtractsIngredientsAndInstructionsSeparately() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val results = RawSections()
        val reordered1DLines = Ocr3SpatialLineReorderer.reorder2DLines(lines2DScan, vocabulary, results)
        val sections = Ocr3Categorizer.categorize(reordered1DLines, vocabulary)

        val ingredients = sections.rawIngredientsList
        val instructions = sections.rawInstructionsList

        assertWithMessage("Le poireau doit être capturé dans les ingrédients").that(ingredients.any { it.contains("poireau", ignoreCase = true) }).isTrue()
        assertWithMessage("Le beurre doit être capturé dans les ingrédients").that(ingredients.any { it.contains("beurre", ignoreCase = true) }).isTrue()
        assertWithMessage("Le roquefort doit être capturé dans les ingrédients").that(ingredients.any { it.contains("roquefort", ignoreCase = true) }).isTrue()

        assertWithMessage("La consigne 'Épluchez' ne doit pas fuiter dans les ingrédients").that(ingredients.none { it.contains("Épluchez", ignoreCase = true) }).isTrue()
        assertWithMessage("La consigne 'Épluchez' doit figurer dans les instructions").that(instructions.any { it.contains("Épluchez", ignoreCase = true) }).isTrue()
    }
}
