package com.example.chiefinventory.utils

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito

class Ocr3CategorizerReappearanceTest {

    private lateinit var mockedLog: MockedStatic<Log>
    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        mockedLog = Mockito.mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.d(anyString(), anyString()) }.thenAnswer { invocation ->
            println("LOG [${invocation.getArgument<String>(0)}]: ${invocation.getArgument<String>(1)}")
            0
        }

        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        if (::mockedLog.isInitialized) {
            mockedLog.close()
        }
    }

    private fun assertIngredientDecomposition(
        rawLine: String,
        expectedName: String,
        expectedQty: Double?,
        expectedUnit: String?,
        expectedInfo: String?
    ) {
        val parsed = IngredientParser.parse(rawLine)
        assertWithMessage("Nom incorrect pour '$rawLine'").that(parsed.name).isEqualTo(expectedName)
        assertWithMessage("Quantité incorrecte pour '$rawLine'").that(parsed.quantity).isEqualTo(expectedQty)
        assertWithMessage("Unité incorrecte pour '$rawLine'").that(parsed.unit).isEqualTo(expectedUnit)
        assertWithMessage("Info supp incorrecte pour '$rawLine'").that(parsed.supplementalInfo).isEqualTo(expectedInfo)
    }

    @Test
    fun `test 1 - reapparition simple sans quantite`() {
        val lines = listOf("2 tomates", "les tomates")
        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).contains("2 tomates")
        assertThat(result.rawInstructionsList).contains("les tomates")

        assertIngredientDecomposition("tomates", "tomates", null, null, null)
    }

    @Test
    fun `test 1b - reapparition simple avec quantite`() {
        val lines = listOf("200 g de tomates", "les tomates")
        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).contains("200 g de tomates")
        assertThat(result.rawInstructionsList).contains("les tomates")

        assertIngredientDecomposition("200 g de tomates", "tomates", 200.0, "g", null)
    }

    @Test
    fun `test 2 - reapparition partielle (citron dans jus de citron)`() {
        val lines = listOf("le jus de 1/2 citron", "citron")
        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).contains("le jus de 1/2 citron")
        assertThat(result.rawInstructionsList).contains("citron")

        assertIngredientDecomposition("le jus de 1/2 citron", "citron", 0.5, null, "le jus de")
    }

    @Test
    fun `test 2b - reapparition via jus (jus dans jus de citron)`() {
        val lines = listOf("le jus de 1/2 citron", "ajoutez le jus")
        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).contains("le jus de 1/2 citron")
        assertThat(result.rawInstructionsList).contains("ajoutez le jus")

        assertIngredientDecomposition("le jus de 1/2 citron", "citron", 0.5, null, "le jus de")
    }

    @Test
    fun `test 3 - scenario reel complexe avec accents et plusieurs mots`() {
        val lines = listOf(
            "1 c. à soupe de câpres",
            "le jus de 1/2 citron",
            "sel et poivre",
            "citron, les câpres, du sel et du"
        )

        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).doesNotContain("citron, les câpres, du sel et du")
        assertThat(result.rawInstructionsList).contains("citron, les câpres, du sel et du")

        assertIngredientDecomposition("1 c. à soupe de câpres", "câpres", 1.0, "c. à soupe", null)
    }

    @Test
    fun `test 4 - scenario reel complexe avec accents et plusieurs mots et passage a la ligne`() {
        val lines = listOf(
            "1 c. à soupe de câpres",
            "le jus",
            "de 1/2 citron",
            "sel et poivre",
            "citron, les câpres, du sel et du"
        )

        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).doesNotContain("citron, les câpres, du sel et du")
        assertThat(result.rawInstructionsList).contains("citron, les câpres, du sel et du")
    }

    @Test
    fun `test 5 - scenario multi-lignes surimi`() {
        val lines = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de câpres",
            "2 c. à soupe de persil haché",
            "2 œufs durs",
            "4 toasts",
            "salade de blé",
            "le jus de 1/2 citron",
            "sel et poivre"
        )

        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).hasSize(lines.size)

        assertIngredientDecomposition(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "surimi ou de véritable crabe",
            120.0,
            "g",
            "poisson au goût de crabe"
        )
        assertIngredientDecomposition("2 œufs durs", "œufs durs", 2.0, null, null)
        assertIngredientDecomposition("le jus de 1/2 citron", "citron", 0.5, null, "le jus de")
    }

    @Test
    fun `test 6 - scenario complet surimi avec instructions`() {
        val lines = listOf(
            "120 g de surimi (poisson au goût de crabe) ou de véritable crabe",
            "2 c. à soupe de mayonnaise",
            "1 c. à soupe de câpres",
            "2 c. à soupe de persil haché",
            "2 œufs durs",
            "4 toasts",
            "salade de blé",
            "le jus de 1/2 citron",
            "sel et poivre",
            "Salade de surimi",
            "• Hachez finement le surimi ou",
            "le crabe ainsi que les œufs",
            "durs.",
            "• Mélangez avec la",
            "mayonnaise, le persil, le jus de",
            "citron, les câpres, du sel et du",
            "poivre.",
            "• Disposez un peu de salade de blé en forme de cercle sur une assiette et déposez-y la salade de surimi.",
            "Assiette de présentation disponible dans les Maxi GB",
            "• Servez avec les toasts."
        )

        val result = Ocr3Categorizer.categorize(lines, vocabulary)

        assertThat(result.rawIngredientsList).hasSize(9)
        assertThat(result.rawInstructionsList).hasSize(7)

        assertThat(result.rawInstructionsList.any { it.startsWith("Hachez finement le surimi") }).isTrue()
        assertThat(result.rawInstructionsList).contains("Disposez un peu de salade de blé en forme de cercle sur une assiette et déposez-y la salade de surimi.")
        assertThat(result.rawInstructionsList).contains("Servez avec les toasts.")
        assertThat(result.rawIngredientsList).doesNotContain("Hachez finement le surimi ou")
    }

    @Test
    fun `categorize should pull back spoon unit ingredient located in instruction section`() {
        val input = listOf(
            "INGRÉDIENTS",
            "200 g de crevettes",
            "sel et poivre",
            "Mélanger les crevettes avec la sauce.",
            "1 c. à soupe de coriandre fraîche hachée"
        )

        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.rawIngredientsList).contains("1 c. à soupe de coriandre fraîche hachée")
        assertThat(result.rawInstructionsList).doesNotContain("1 c. à soupe de coriandre fraîche hachée")
    }
}
