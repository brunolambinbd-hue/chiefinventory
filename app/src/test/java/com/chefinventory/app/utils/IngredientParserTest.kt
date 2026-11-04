package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import java.io.File

class IngredientParserTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    private fun assertIngredient(
        input: String,
        expectedName: String,
        expectedQty: Double?,
        expectedUnit: String?,
        expectedInfo: String?
    ) {
        val normalized = Ocr1Corrector.normalize(input)
        val result = IngredientParser.parse(normalized)
        assertWithMessage("Nom incorrect pour '$input'").that(result.name).isEqualTo(expectedName)
        assertWithMessage("Quantité incorrecte pour '$input'").that(result.quantity).isEqualTo(expectedQty)
        assertWithMessage("Unité incorrecte pour '$input'").that(result.unit).isEqualTo(expectedUnit)
        assertWithMessage("Info supp incorrecte pour '$input'").that(result.supplementalInfo).isEqualTo(expectedInfo)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractRestText_DirectCallAllBranches() {
        // 1. null -> empty string
        assertThat(IngredientParser.extractRestText(null)).isEmpty()

        // 2. whitespace string -> empty string
        assertThat(IngredientParser.extractRestText("   ")).isEmpty()

        // 3. valid string -> trimmed string
        assertThat(IngredientParser.extractRestText(" g de farine ")).isEqualTo("g de farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExhaustiveBranchCoverage_NonInitialMatches() {
        // 1. Non-initial range match (range.first != 0)
        val resRangeNonInit = IngredientParser.parse("Farine 10 à 15 g")
        assertThat(resRangeNonInit.name).contains("Farine 10 à 15 g")

        // 2. Non-initial vague match (range.first != 0)
        val resVagueNonInit = IngredientParser.parse("Farine quelques poignées")
        assertThat(resVagueNonInit.name).contains("Farine quelques poignées")

        // 3. Non-clean fraction prefix (isCleanPrefix = false)
        val resFracNonClean = IngredientParser.parse("Farine 1/2 tasse")
        assertThat(resFracNonClean.quantity).isNull()

        // 4. Jus fraction prefix (isJusPrefix = true)
        val resJusFrac = IngredientParser.parse("le jus de 1/2 citron")
        assertThat(resJusFrac.quantity).isEqualTo(0.5)

        // 5. Adjective prefix not at index 0 in vague match
        val resVagueAdjNotStart = IngredientParser.parse("des feuilles petites de chicon")
        assertThat(resVagueAdjNotStart.unit).isEqualTo("feuilles")

        // 6. Adjective prefix not at index 0 in standard match
        val resStdAdjNotStart = IngredientParser.parse("2 tomates grosses")
        assertThat(resStdAdjNotStart.quantity).isEqualTo(2.0)
        assertThat(resStdAdjNotStart.name).isEqualTo("tomates grosses")

        // 7. Quantity without unit or text (rawGroup2 == null)
        val resNoRest = IngredientParser.parse("250")
        assertThat(resNoRest.quantity).isEqualTo(250.0)
        assertThat(resNoRest.name).isEmpty()

        // 8. Empty string
        val resBlank = IngredientParser.parse("   ")
        assertThat(resBlank.name).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testAtomizedVagueQuantityAndAdjectiveVariations() {
        data class VagueTestCase(
            val input: String,
            val expectedName: String,
            val expectedUnit: String?,
            val expectedInfo: String?
        )

        val testCases = listOf(
            VagueTestCase("quelques branches d'aneth", "aneth", "branches", "quelques"),
            VagueTestCase("des petites feuilles de chicon", "chicon", "feuilles", "petites des"),
            VagueTestCase("des petites feuilles de chicon (frais)", "chicon", "feuilles", "petites des frais"),
            VagueTestCase("plusieurs gousses d'ail", "ail", "gousses", "plusieurs")
        )

        for (tc in testCases) {
            val result = IngredientParser.parse(tc.input)
            assertWithMessage("Échec sur le nom pour '${tc.input}'").that(result.name).isEqualTo(tc.expectedName)
            assertWithMessage("Échec sur l'unité pour '${tc.input}'").that(result.unit).isEqualTo(tc.expectedUnit)
            assertWithMessage("Échec sur info pour '${tc.input}'").that(result.supplementalInfo).isEqualTo(tc.expectedInfo)
        }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFractionWithZeroDenominator_ReturnsOriginalText() {
        val result = IngredientParser.parse("1/0 verre d'eau")
        assertThat(result.quantity).isNull()
        assertThat(result.name).isEqualTo("1/0 verre d'eau")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFractionWithPrefixAndParenthesisAndAdjectiveElseBranch() {
        val resultFraction = IngredientParser.parse("le jus de 1/2 citron (biologique)")
        assertThat(resultFraction.quantity).isEqualTo(0.5)
        assertThat(resultFraction.name).isEqualTo("citron")
        assertThat(resultFraction.supplementalInfo).isEqualTo("le jus de biologique")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParsedIngredientDataClassAndLoadDefaultUnitsError() {
        val parsedDefault = IngredientParser.ParsedIngredient(name = "farine")
        assertThat(parsedDefault.quantity).isNull()
        assertThat(parsedDefault.unit).isNull()
        assertThat(parsedDefault.supplementalInfo).isNull()

        // Access defaultUnits while production vocabulary is cached
        val unitInfo = IngredientParser.findUnit("g de farine")
        assertThat(unitInfo.unit).isEqualTo("g")

        // Test error handling for isolated directory
        val isolatedDir = File(System.getProperty("java.io.tmpdir"), "isolated_test_dir_parser_999")
        isolatedDir.mkdirs()
        val emptyUnits = IngredientParser.loadDefaultUnits(isolatedDir)
        assertThat(emptyUnits).isEmpty()
        isolatedDir.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `decomposition tests for various ingredients`() {
        assertIngredient("200 g de tomates", "tomates", 200.0, "g", null)
        assertIngredient("le jus de 1/2 citron", "citron", 0.5, null, "le jus de")
        assertIngredient("1 c. à soupe de câpres", "câpres", 1.0, "c. à soupe", null)
        assertIngredient("2 grosses tomates", "tomates", 2.0, null, "grosses")
        assertIngredient("quelques feuilles de basilic", "basilic", null, "feuilles", "quelques")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse plant units branches and leaves`() {
        assertIngredient("2 feuilles de laurier", "laurier", 2.0, "feuilles", null)
        assertIngredient("1 branche de thym", "thym", 1.0, "branche", null)
        assertIngredient("quelques branches d'aneth", "aneth", null, "branches", "quelques")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `defaultUnits should dynamically load essential culinary units from XML resources`() {
        val xmlUnits = vocabulary.culinaryMeasurementUnits
        val defaultUnits = IngredientParser.defaultUnits

        assertThat(defaultUnits).containsExactlyElementsIn(xmlUnits)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `preClean basic normalizations`() {
        assertThat(Ocr1Corrector.normalize("  ± 100 g  ")).isEqualTo("100 g")
        assertThat(Ocr1Corrector.normalize("200 g +/-")).isEqualTo("200 g")
        assertThat(Ocr1Corrector.normalize("+- 50 g")).isEqualTo("50 g")
        assertThat(Ocr1Corrector.normalize("environ 100 g")).isEqualTo("100 g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `preClean OCR corrections`() {
        assertThat(Ocr1Corrector.normalize("| citron")).isEqualTo("1 citron")
        assertThat(Ocr1Corrector.normalize("l/2")).isEqualTo("1/2")
        assertThat(Ocr1Corrector.normalize("1 e citron")).isEqualTo("le citron")
        assertThat(Ocr1Corrector.normalize("1'ail")).isEqualTo("l'ail")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse fraction handling`() {
        val result = IngredientParser.parse("1/2 l d'eau")
        assertThat(result.quantity).isEqualTo(0.5)
        assertThat(result.unit).isEqualTo("l")
        assertThat(result.name).isEqualTo("eau")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse range handling`() {
        val result = IngredientParser.parse("2 à 3 pommes")
        assertThat(result.quantity).isEqualTo(3.0)
        assertThat(result.name).isEqualTo("pommes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse standard numeric quantity with unit`() {
        val result = IngredientParser.parse("250 g de sucre")
        assertThat(result.quantity).isEqualTo(250.0)
        assertThat(result.unit).isEqualTo("g")
        assertThat(result.name).isEqualTo("sucre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse unit precedence longest match`() {
        val result = IngredientParser.parse("2 c. à soupe de miel")
        assertThat(result.unit).isEqualTo("c. à soupe")
        assertThat(result.name).isEqualTo("miel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse decimal separator`() {
        val result1 = IngredientParser.parse("1,5 kg")
        assertThat(result1.quantity).isEqualTo(1.5)
        val result2 = IngredientParser.parse("1.5 kg")
        assertThat(result2.quantity).isEqualTo(1.5)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `parse edge cases`() {
        val resultEmpty = IngredientParser.parse("")
        assertThat(resultEmpty.name).isEmpty()
        assertThat(resultEmpty.quantity).isNull()

        val resultQtyOnly = IngredientParser.parse("2")
        assertThat(resultQtyOnly.quantity).isEqualTo(2.0)
        assertThat(resultQtyOnly.name).isEmpty()
    }
}
