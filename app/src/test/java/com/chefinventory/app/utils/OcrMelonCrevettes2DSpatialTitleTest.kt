package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant la fusion 2D spatiale de deux blocs de titres côte-à-côte non contigus en 1D dans [Ocr3CategorizerTitleExtractor].
 */
class OcrMelonCrevettes2DSpatialTitleTest {

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
    fun testExtractTitle_WithNonContiguous1DSpatial2DBlocks_AssemblesAndRepairsTitleCleanly() {
        // En 1D ML Kit, un autre bloc (comme INGRÉDIENTS) a été inséré entre le bloc gauche et le bloc droit du titre
        val lines = listOf(
            "bllelon aux",
            "INGRÉDIENTS",
            "Crevettes et au jambon séché",
            "1 melon Galia",
            "8 tranches de jambon"
        )

        val lines2D = listOf(
            OcrTextLine("bllelon aux", top = 746, left = 566, bottom = 947, right = 1098),
            OcrTextLine("INGRÉDIENTS", top = 130, left = 112, bottom = 175, right = 405),
            OcrTextLine("Crevettes et au jambon séché", top = 757, left = 1123, bottom = 918, right = 2315),
            OcrTextLine("1 melon Galia", top = 268, left = 99, bottom = 304, right = 423)
        )

        val result = Ocr3CategorizerTitleExtractor.extractTitle(lines, vocabulary, lines2D)
        val beautifiedTitle = Ocr5BeautifierTitle.beautifyTitle(result.title)

        assertWithMessage("Le titre séparé visuellement en 2 blocs côte-à-côte (top 746 vs 757) doit être fusionné en 2D et réparé en 'Melon aux crevettes et au jambon séché'")
            .that(beautifiedTitle)
            .isEqualTo("Melon aux crevettes et au jambon séché")
    }
}
