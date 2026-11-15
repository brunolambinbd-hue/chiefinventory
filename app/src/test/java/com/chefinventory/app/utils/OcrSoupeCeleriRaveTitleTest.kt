package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant l'extraction exacte du titre en majuscules sur 2 lignes
 * 'SOUPE AU CÉLERI-RAVE, NOIX ET MASCARPONE' dans [RecipeOcrParser].
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
        "Mettez le tout dans une casserole contenant le beurre fondu. Ajoutez les gousses d'ail épluchées et coupées en 2, ainsi que le thym.",
        "Faites revenir 5 min à bon feu, puis arrosez de 1 l d'eau. Joignez les cubes de bouillon émiettés et portez à ébullition. Laissez mijoter 30 min.",
        "Hachez grossièrement les noix et faites-les légèrement griller à sec, dans une poêle.",
        "Ajoutez le mascarpone dans la soupe et mixez finement. Rectifiez l'assaisonnement et servez bien chaud, saupoudré de noix."
    ).joinToString("\n")

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
}
