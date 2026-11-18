package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation 2D vérifiant la capture complète des 7 ingrédients
 * sur la recette à 2 colonnes sans en-tête 'Tortillas de Quinoa aux Épinards'.
 */
class OcrTortillasQuinoaHeaderless2DTest {

    private val lines2DScan = listOf(
        OcrTextLine("TORTILLAS DE QUINOA", top = 716, left = 45, bottom = 754, right = 363),
        OcrTextLine("AUX ÉPINARDS", top = 756, left = 46, bottom = 802, right = 257),
        OcrTextLine("O25 MIN +15 MIN DE CUISSON ASSEZ FACILE OEEE", top = 820, left = 65, bottom = 852, right = 495),
        OcrTextLine("A2PERS, O745 KCAL/POR.", top = 855, left = 48, bottom = 880, right = 275),
        OcrTextLine("NUTR-SCoRE", top = 914, left = 56, bottom = 927, right = 139),
        OcrTextLine("B", top = 939, left = 81, bottom = 962, right = 97),
        OcrTextLine("•4 eufs", top = 993, left = 53, bottom = 1024, right = 132),
        OcrTextLine("•170 g d'epinards", top = 1026, left = 56, bottom = 1058, right = 218),
        OcrTextLine("• l oignon", top = 1062, left = 62, bottom = 1092, right = 144),
        OcrTextLine("•150 g de quinoa bio", top = 1094, left = 55, bottom = 1128, right = 244),
        OcrTextLine("5cdsdhuile d'olive", top = 986, left = 373, bottom = 1017, right = 545),
        OcrTextLine("•Poivre", top = 1025, left = 347, bottom = 1050, right = 416),
        OcrTextLine("• Sel", top = 1058, left = 348, bottom = 1085, right = 395),
        OcrTextLine("O Faites revenir l'oignon haché à feu douN", top = 1176, left = 88, bottom = 1216, right = 528)
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
    fun testTortillasQuinoa_SpatialLineReorderer_ExtractsAllSevenIngredients() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val results = RawSections()
        val reordered1DLines = Ocr3SpatialLineReorderer.reorder2DLines(lines2DScan, vocabulary, results)
        val sections = Ocr3Categorizer.categorize(reordered1DLines, vocabulary)

        val ingredients = sections.rawIngredientsList

        assertWithMessage("Les œufs (Colonne 1) doivent être capturés").that(ingredients.any { it.contains("eufs", ignoreCase = true) || it.contains("œufs", ignoreCase = true) }).isTrue()
        assertWithMessage("Les épinards (Colonne 1) doivent être capturés").that(ingredients.any { it.contains("epinards", ignoreCase = true) || it.contains("épinards", ignoreCase = true) }).isTrue()
        assertWithMessage("L'oignon (Colonne 1) doit être capturé").that(ingredients.any { it.contains("oignon", ignoreCase = true) }).isTrue()
        assertWithMessage("Le quinoa (Colonne 1) doit être capturé").that(ingredients.any { it.contains("quinoa", ignoreCase = true) }).isTrue()

        assertWithMessage("L'huile d'olive (Colonne 2) doit être capturée").that(ingredients.any { it.contains("huile", ignoreCase = true) }).isTrue()
        assertWithMessage("Le poivre (Colonne 2) doit être capturé").that(ingredients.any { it.contains("poivre", ignoreCase = true) }).isTrue()
        assertWithMessage("Le sel (Colonne 2) doit être capturé").that(ingredients.any { it.contains("sel", ignoreCase = true) }).isTrue()
        assertWithMessage("L'instruction 'd'eau si nécessaire' ne doit PAS figurer dans les ingrédients").that(ingredients.none { it.contains("nécessaire", ignoreCase = true) }).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTortillasQuinoa_IsNotClassifiedAsNarrative_AndExtractsIngredientsIn2DPipeline() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawLines = lines2DScan.map { it.text }
        val isNarrative = Ocr3NarrativeCategorizer.isNarrativeInstructionOnlyRecipe(rawLines, vocabulary)

        assertWithMessage("La recette Tortillas de Quinoa ne doit PAS être classée en mode narratif")
            .that(isNarrative)
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testTortillasQuinoa_CameraScanWithBulletArtifactO_MustExtractIngredientsAndInstructionsCleanly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = RecipeOcrParser.parse(lines2DScan, vocabulary)

        val ingredients = result.ingredients?.split("\n") ?: emptyList()
        val instructions = result.instructions?.split("\n") ?: emptyList()

        assertWithMessage("Les instructions ne doivent PAS fuiter dans les ingrédients")
            .that(ingredients.none { it.contains("Faites revenir", ignoreCase = true) })
            .isTrue()

        assertWithMessage("La consigne 'Faites revenir' doit figurer dans les instructions")
            .that(instructions.any { it.contains("Faites revenir", ignoreCase = true) })
            .isTrue()
    }
}
