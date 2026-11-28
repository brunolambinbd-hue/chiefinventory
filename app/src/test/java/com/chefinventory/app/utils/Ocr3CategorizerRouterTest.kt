package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD d'isolation de [Ocr3CategorizerRouter] (Aiguilleur Étape 3)
 * vérifiant l'orientations des 4 types de recettes vers leurs lecteurs respectifs.
 */
class Ocr3CategorizerRouterTest {

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
    fun testRouter_OneColumnRecipe_RoutesToClassicOneColumn() {
        val linesWithoutTitle = listOf(
            "INGRÉDIENTS",
            "• 4 tomates",
            "• 2 c. à soupe d'huile",
            "INSTRUCTIONS",
            "• Coupez les tomates en dés."
        )

        val sections = Ocr3CategorizerRouter.routeAndCategorize(linesWithoutTitle, emptyList(), linesWithoutTitle, vocabulary)

        assertThat(sections.rawIngredientsList).containsAtLeast("4 tomates", "2 c. à soupe d'huile")
        assertThat(sections.rawInstructionsList).contains("Coupez les tomates en dés.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRouter_NarrativeRecipe_RoutesToOneColumnOnlyInstructions() {
        val linesWithoutTitle = listOf(
            "Faites revenir 200 g de poulet dans 2 c. à soupe d'huile d'olive pendant 10 min.",
            "Servez chaud."
        )

        val sections = Ocr3CategorizerRouter.routeAndCategorize(linesWithoutTitle, emptyList(), linesWithoutTitle, vocabulary)

        assertThat(sections.rawInstructionsList).hasSize(2)
        assertThat(sections.rawIngredientsList).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateReaderType_IsolatesEvaluationCleanly() {
        val linesWithoutTitle = listOf(
            "INGRÉDIENTS",
            "• 4 tomates",
            "INSTRUCTIONS",
            "• Coupez les tomates."
        )

        val (readerType, _) = Ocr3CategorizerRouter.evaluateReaderType(linesWithoutTitle, emptyList(), vocabulary)
        assertThat(readerType).isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INGREDIENT_FIRST)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testReaderTypeEnumAndEmptyLines2D_Coverage() {
        val types = Ocr3CategorizerRouter.ReaderType.entries
        assertThat(types).hasSize(4)
        assertThat(Ocr3CategorizerRouter.ReaderType.valueOf("ONE_COLUMN_INSTRUCTION_FIRST"))
            .isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INSTRUCTION_FIRST)

        val (type, lines2D) = Ocr3CategorizerRouter.evaluateReaderType(listOf("4 tomates"), emptyList(), vocabulary)
        assertThat(type).isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INGREDIENT_FIRST)
        assertThat(lines2D).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateReaderType_TwoColumnsWithoutIngredientsHeader_RoutesToTwoColumns() {
        val lines2D = listOf(
            OcrTextLine("4 tomates", top = 100, left = 100, bottom = 400, right = 400),
            OcrTextLine("Coupez les tomates", top = 100, left = 500, bottom = 400, right = 800)
        )
        val linesWithoutTitle = lines2D.map { it.text }

        val (readerType, processed2D) = Ocr3CategorizerRouter.evaluateReaderType(linesWithoutTitle, lines2D, vocabulary)
        assertThat(readerType).isEqualTo(Ocr3CategorizerRouter.ReaderType.TWO_COLUMNS_INGREDIENT_FIRST)
        assertThat(processed2D).isNotEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsTitleCandidateLine_AllAtomicBranches() {
        val linesWithoutTitle = listOf("salade de tomates", "coupez les tomates")

        // 1. Blank text -> false
        assertWithMessage("Une chaîne de texte vide ne doit pas être candidate titre")
            .that(Ocr3CategorizerRouter.isTitleCandidateLine("", linesWithoutTitle))
            .isFalse()

        // 2. Exact match -> false
        assertWithMessage("Un texte égal à une ligne courante ne doit pas être conservé comme titre 2D indépendant")
            .that(Ocr3CategorizerRouter.isTitleCandidateLine("salade de tomates", linesWithoutTitle))
            .isFalse()

        // 3. Target contains cleanText -> false
        assertWithMessage("Un fragment contenu dans une ligne courante ne doit pas être conservé comme titre 2D")
            .that(Ocr3CategorizerRouter.isTitleCandidateLine("tomates", linesWithoutTitle))
            .isFalse()

        // 4. CleanText contains target -> false
        assertWithMessage("Un texte englobant une ligne courante ne doit pas être conservé comme titre 2D")
            .that(Ocr3CategorizerRouter.isTitleCandidateLine("la salade de tomates maison", linesWithoutTitle))
            .isFalse()

        // 5. No match in linesWithoutTitle -> true
        assertWithMessage("Un titre autonome absent du corps de texte doit être retenu comme candidat titre 2D")
            .that(Ocr3CategorizerRouter.isTitleCandidateLine("poulet rôti aux herbes", linesWithoutTitle))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateReaderType_OneColumnSmallCoordinatesNoHeader_SelectsOneColumn() {
        val lines2D = listOf(
            OcrTextLine("sel", left = 10, right = 50, top = 10, bottom = 20)
        )
        val (readerType, _) = Ocr3CategorizerRouter.evaluateReaderType(
            linesWithoutTitle = listOf("sel"),
            lines2D = lines2D,
            vocabulary = vocabulary
        )
        assertWithMessage("Un scan 1 colonne avec petites coordonnées et sans en-tête doit sélectionner ONE_COLUMN_INGREDIENT_FIRST")
            .that(readerType)
            .isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INGREDIENT_FIRST)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testEvaluateReaderType_Real2DScanSubConditions_SelectsTwoColumns() {
        // Sub-condition 1: right > 300
        val linesRight = listOf(OcrTextLine("sel", left = 10, right = 350, top = 10, bottom = 20))
        val (resRight, _) = Ocr3CategorizerRouter.evaluateReaderType(listOf("sel"), linesRight, vocabulary)
        assertThat(resRight).isEqualTo(Ocr3CategorizerRouter.ReaderType.TWO_COLUMNS_INGREDIENT_FIRST)

        // Sub-condition 2: left > 300
        val linesLeft = listOf(OcrTextLine("sel", left = 350, right = 400, top = 10, bottom = 20))
        val (resLeft, _) = Ocr3CategorizerRouter.evaluateReaderType(listOf("sel"), linesLeft, vocabulary)
        assertThat(resLeft).isEqualTo(Ocr3CategorizerRouter.ReaderType.TWO_COLUMNS_INGREDIENT_FIRST)

        // Sub-condition 3: bottom > 300
        val linesBottom = listOf(OcrTextLine("sel", left = 10, right = 50, top = 10, bottom = 350))
        val (resBottom, _) = Ocr3CategorizerRouter.evaluateReaderType(listOf("sel"), linesBottom, vocabulary)
        assertThat(resBottom).isEqualTo(Ocr3CategorizerRouter.ReaderType.TWO_COLUMNS_INGREDIENT_FIRST)

        // Empty lines2D -> ONE_COLUMN_INGREDIENT_FIRST
        val (resEmpty, _) = Ocr3CategorizerRouter.evaluateReaderType(listOf("sel"), emptyList(), vocabulary)
        assertThat(resEmpty).isEqualTo(Ocr3CategorizerRouter.ReaderType.ONE_COLUMN_INGREDIENT_FIRST)
    }
}
