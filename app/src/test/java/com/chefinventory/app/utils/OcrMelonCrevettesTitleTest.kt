package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la fusion 2D et la réparation de lettrine du titre 'bllelon aux Crevettes et au jambon séché' ([Ocr3CategorizerTitleExtractor]).
 */
class OcrMelonCrevettesTitleTest {

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
    fun testExtractTitle_WithTitleConnectorOnLine1AndLettrineTypo_CombinesAndRepairsTitleCleanly() {
        val lines = listOf(
            "bllelon aux",
            "Crevettes et au jambon séché",
            "INGRÉDIENTS",
            "1 melon Galia",
            "8 tranches de jambon"
        )

        val lines2D = listOf(
            OcrTextLine("bllelon aux", top = 746, left = 566, bottom = 947, right = 1098), // 201px height!
            OcrTextLine("Crevettes et au jambon séché", top = 757, left = 1123, bottom = 918, right = 2315), // 161px height!
            OcrTextLine("INGRÉDIENTS", top = 130, left = 112, bottom = 175, right = 405)
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary, lines2D)
        val beautifiedTitle = Ocr5BeautifierTitle.beautifyTitle(result.title)

        assertWithMessage("Le titre multi-lignes avec lettrine 'bllelon' et connecteur 'aux' doit être fusionné et réparé en 'Melon aux crevettes et au jambon séché'")
            .that(beautifiedTitle)
            .isEqualTo("Melon aux crevettes et au jambon séché")
    }
}
