package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

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
        val normalized = Ocr1Normalizer.normalize(input)
        val result = IngredientParser.parse(normalized)
        assertWithMessage("Nom incorrect pour '$input'").that(result.name).isEqualTo(expectedName)
        assertWithMessage("Quantité incorrecte pour '$input'").that(result.quantity).isEqualTo(expectedQty)
        assertWithMessage("Unité incorrecte pour '$input'").that(result.unit).isEqualTo(expectedUnit)
        assertWithMessage("Info supp incorrecte pour '$input'").that(result.supplementalInfo).isEqualTo(expectedInfo)
    }

    @Test
    fun `decomposition tests for various ingredients`() {
        assertIngredient("200 g de tomates", "tomates", 200.0, "g", null)
        assertIngredient("le jus de 1/2 citron", "citron", 0.5, null, "le jus de")
        assertIngredient("1 c. à soupe de câpres", "câpres", 1.0, "c. à soupe", null)
        assertIngredient("2 grosses tomates", "tomates", 2.0, null, "grosses")
        assertIngredient("quelques feuilles de basilic", "basilic", null, "feuilles", "quelques")
    }

    @Test
    fun `parse plant units branches and leaves`() {
        assertIngredient("2 feuilles de laurier", "laurier", 2.0, "feuilles", null)
        assertIngredient("1 branche de thym", "thym", 1.0, "branche", null)
        assertIngredient("quelques branches d'aneth", "aneth", null, "branches", "quelques")
    }

    @Test
    fun `parse vague quantity and adjective extraction`() {
        assertIngredient("des petites feuilles de chicon", "chicon", null, "feuilles", "petites des")
    }

    @Test
    fun `defaultUnits should dynamically load essential culinary units from XML resources`() {
        val xmlUnits = vocabulary.culinaryMeasurementUnits
        val defaultUnits = IngredientParser.defaultUnits

        assertThat(defaultUnits).containsExactlyElementsIn(xmlUnits)
    }

    @Test
    fun `preClean basic normalizations`() {
        assertThat(Ocr1Normalizer.normalize("  ± 100 g  ")).isEqualTo("100 g")
        assertThat(Ocr1Normalizer.normalize("200 g +/-")).isEqualTo("200 g")
        assertThat(Ocr1Normalizer.normalize("+- 50 g")).isEqualTo("50 g")
        assertThat(Ocr1Normalizer.normalize("environ 100 g")).isEqualTo("100 g")
    }

    @Test
    fun `preClean OCR corrections`() {
        assertThat(Ocr1Normalizer.normalize("| citron")).isEqualTo("1 citron")
        assertThat(Ocr1Normalizer.normalize("l/2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("1 e citron")).isEqualTo("le citron")
        assertThat(Ocr1Normalizer.normalize("1'ail")).isEqualTo("l'ail")
    }

    @Test
    fun `parse fraction handling`() {
        val result = IngredientParser.parse("1/2 l d'eau")
        assertThat(result.quantity).isEqualTo(0.5)
        assertThat(result.unit).isEqualTo("l")
        assertThat(result.name).isEqualTo("eau")
    }

    @Test
    fun `parse range handling`() {
        val result = IngredientParser.parse("2 à 3 pommes")
        assertThat(result.quantity).isEqualTo(3.0)
        assertThat(result.name).isEqualTo("pommes")
    }

    @Test
    fun `parse standard numeric quantity with unit`() {
        val result = IngredientParser.parse("250 g de sucre")
        assertThat(result.quantity).isEqualTo(250.0)
        assertThat(result.unit).isEqualTo("g")
        assertThat(result.name).isEqualTo("sucre")
    }

    @Test
    fun `parse unit precedence longest match`() {
        val result = IngredientParser.parse("2 c. à soupe de miel")
        assertThat(result.unit).isEqualTo("c. à soupe")
        assertThat(result.name).isEqualTo("miel")
    }

    @Test
    fun `parse decimal separator`() {
        val result1 = IngredientParser.parse("1,5 kg")
        assertThat(result1.quantity).isEqualTo(1.5)
        val result2 = IngredientParser.parse("1.5 kg")
        assertThat(result2.quantity).isEqualTo(1.5)
    }

    @Test
    fun `parse edge cases`() {
        val resultEmpty = IngredientParser.parse("")
        assertThat(resultEmpty.name).isEmpty()
        assertThat(resultEmpty.quantity).isNull()

        val resultQtyOnly = IngredientParser.parse("2")
        assertThat(resultQtyOnly.quantity).isEqualTo(2.0)
        assertThat(resultQtyOnly.name).isEmpty()
    }
}
