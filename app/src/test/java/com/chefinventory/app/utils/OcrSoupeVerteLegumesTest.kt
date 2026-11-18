package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation vérifiant la capture complète des 2 colonnes d'ingrédients
 * sur la recette à 2 colonnes sans en-tête 'Soupe verte aux légumes d'hiver et au roquefort'.
 */
class OcrSoupeVerteLegumesTest {

    private val lines2DScan = listOf(
        OcrTextLine("SOUPE VERTE AUX LÉGUMES", top = 32, left = 101, bottom = 183, right = 1479),
        OcrTextLine("D'HIVER ET AU ROQUEFORT", top = 213, left = 101, bottom = 326, right = 1424),
        OcrTextLine("O 15 MIN + 30 MIN DE CUISSON FACILE EEE", top = 399, left = 93, bottom = 478, right = 1391),
        OcrTextLine("A4PERS. 249 KCAUPOR.", top = 497, left = 155, bottom = 569, right = 833),
        OcrTextLine("NUTRI-ScORE", top = 689, left = 134, bottom = 722, right = 399),
        OcrTextLine("A", top = 779, left = 134, bottom = 834, right = 179),
        OcrTextLine("•l poireau", top = 950, left = 113, bottom = 1042, right = 463),
        OcrTextLine("• 3 oignons 3", top = 1061, left = 125, bottom = 1166, right = 559),
        OcrTextLine("• 200 g d'epinards", top = 1172, left = 127, bottom = 1279, right = 671),
        OcrTextLine("• 2 pommes de terre multi-", top = 1288, left = 108, bottom = 1386, right = 907),
        OcrTextLine("Usages (+ 215 g) .", top = 1396, left = 170, bottom = 1507, right = 792),
        OcrTextLine("• 100 g de roquefort", top = 1512, left = 126, bottom = 1609, right = 710),
        OcrTextLine("•20 g de beurre", top = 937, left = 1097, bottom = 1039, right = 1571),
        OcrTextLine("• 2 cubes de bouillon", top = 1055, left = 1086, bottom = 1149, right = 1712),
        OcrTextLine("de légumes", top = 1160, left = 1142, bottom = 1261, right = 1481),
        OcrTextLine(".1 bouquet garni", top = 1279, left = 1093, bottom = 1370, right = 1633),
        OcrTextLine("•Poivre", top = 1398, left = 1084, bottom = 1478, right = 1324),
        OcrTextLine("• Sel", top = 1506, left = 1108, bottom = 1596, right = 1243),
        OcrTextLine("O Épluchez les pommes de terre et taillez-", top = 1741, left = 104, bottom = 1846, right = 1826)
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
    fun testSoupeVerteLegumes_SpatialLineReorderer_ExtractsAllTwoColumnIngredients() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val results = RawSections()
        val reordered1DLines = Ocr3SpatialLineReorderer.reorder2DLines(lines2DScan, vocabulary, results)
        val sections = Ocr3Categorizer.categorize(reordered1DLines, vocabulary)

        val ingredients = sections.rawIngredientsList

        assertWithMessage("Le poireau (Colonne 1) doit être capturé").that(ingredients.any { it.contains("poireau", ignoreCase = true) }).isTrue()
        assertWithMessage("Les oignons (Colonne 1) doivent être capturés").that(ingredients.any { it.contains("oignons", ignoreCase = true) }).isTrue()
        assertWithMessage("Les épinards (Colonne 1) doivent être capturés").that(ingredients.any { it.contains("epinards", ignoreCase = true) || it.contains("épinards", ignoreCase = true) }).isTrue()
        assertWithMessage("Les pommes de terre (Colonne 1) doivent être capturées").that(ingredients.any { it.contains("pommes de terre", ignoreCase = true) }).isTrue()
        assertWithMessage("Le roquefort (Colonne 1) doit être capturé").that(ingredients.any { it.contains("roquefort", ignoreCase = true) }).isTrue()

        assertWithMessage("Le beurre (Colonne 2) doit être capturé").that(ingredients.any { it.contains("beurre", ignoreCase = true) }).isTrue()
        assertWithMessage("Le bouillon (Colonne 2) doit être capturé").that(ingredients.any { it.contains("bouillon", ignoreCase = true) }).isTrue()
        assertWithMessage("Le bouquet garni (Colonne 2) doit être capturé").that(ingredients.any { it.contains("bouquet garni", ignoreCase = true) }).isTrue()
        assertWithMessage("Le poivre (Colonne 2) doit être capturé").that(ingredients.any { it.contains("poivre", ignoreCase = true) }).isTrue()
        assertWithMessage("Le sel (Colonne 2) doit être capturé").that(ingredients.any { it.contains("sel", ignoreCase = true) }).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSpatialLineReorderer_ShortStartVerbInstruction_TriggersInstructionLimitCleanly() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val shortInstructionLines = listOf(
            OcrTextLine("SOUPE AU CÉLERI", top = 50),
            OcrTextLine("• 1 céleri-rave", top = 900),
            OcrTextLine("Épluchez le céleri", top = 1200)
        )

        val results = RawSections()
        val reordered1DLines = Ocr3SpatialLineReorderer.reorder2DLines(shortInstructionLines, vocabulary, results)
        val sections = Ocr3Categorizer.categorize(reordered1DLines, vocabulary)

        assertWithMessage("L'ingrédient 'céleri-rave' doit être dans les ingrédients").that(sections.rawIngredientsList.any { it.contains("céleri") }).isTrue()
        assertWithMessage("La consigne 'Épluchez le céleri' doit être dans les instructions").that(sections.rawInstructionsList.any { it.contains("Épluchez") }).isTrue()
    }
}
