package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant le fonctionnement complet de [Ocr3Categorizer].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerTest {

    private lateinit var vocabulary: OcrVocabulary
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlock0_CategorizationContext_WithEmptyActionVerbs() {
        // Test with empty actionVerbs (covers containsActionRegex == null branch)
        val emptyActionVocab = vocabulary.copy(stepActionKeywords = emptyList())
        val ctxEmpty = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(emptyActionVocab)
        assertThat(ctxEmpty.containsActionRegex).isNull()

        // Test with non-empty actionVerbs (covers containsActionRegex != null branch)
        val ctxLoaded = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)
        assertThat(ctxLoaded.containsActionRegex).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractKeywordsFromText_DirectCallAllBranches() {
        // 1. With non-null supplementalInfo
        val keywords1 = Ocr3Categorizer.extractKeywordsFromText("Farine", "tamisée hachée")
        assertThat(keywords1).contains("farine")

        // 2. With null supplementalInfo
        val keywords2 = Ocr3Categorizer.extractKeywordsFromText("Beurre", null)
        assertThat(keywords2).contains("beurre")

        // 3. Stop words, fillers, units, and short words filtered out
        val keywords3 = Ocr3Categorizer.extractKeywordsFromText("100 grammes de farine pour faire 2 morceaux de sucre", "dans un pot de verre")
        assertThat(keywords3).contains("farine")
        assertThat(keywords3).contains("sucre")
        assertThat(keywords3).doesNotContain("grammes")
        assertThat(keywords3).doesNotContain("pour")
        assertThat(keywords3).doesNotContain("faire")
        assertThat(keywords3).doesNotContain("morceaux")
        assertThat(keywords3).doesNotContain("pot")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorize_WithAndroidResources_CategorizesSuccessfully() {
        val input = listOf("INGRÉDIENTS", "200 g de farine", "INSTRUCTIONS", "Faire cuire au four pendant 15 min.")
        val result = Ocr3Categorizer.categorize(input, context.resources)

        assertThat(result.rawIngredientsList).contains("200 g de farine")
        assertThat(result.rawInstructionsList).contains("Faire cuire au four pendant 15 min.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorize_TechnicalDimensionAndSectionNoneIngredient() {
        val input = listOf("5 cm x 10 cm", "200 g de farine")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.rawInstructionsList).contains("5 cm x 10 cm")
        assertThat(result.rawIngredientsList).contains("200 g de farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCategorize_EndMarkerCatchUpLock() {
        val input = listOf("INGRÉDIENTS", "200 g de farine", "INSTRUCTIONS", "Faire cuire au four pendant 15 min.", "sel et poivre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.rawIngredientsList).contains("200 g de farine")
        assertThat(result.rawIngredientsList).contains("sel et poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `Problem Case 1 - Mixed ingredient line with explicit weight`() {
        val input = listOf("truite (50 g) frais hachés salade")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("Le bloc mixte contenant un poids doit être classé en ingrédient")
            .that(result.rawIngredientsList).contains("truite (50 g) frais hachés salade")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `Problem Case 2 - Isolated food name recovery after connector`() {
        val input = listOf("Ingrédients", "quelques feuilles de", "salade")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("La ligne 'salade' doit être dans les ingrédients")
            .that(result.rawIngredientsList).contains("quelques feuilles de salade")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `Time extraction combined preparation and cooking`() {
        val input = listOf("15 min + 30 min de cuisson")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedPrepTime).isEqualTo("15")
        assertThat(result.detectedCookTime).isEqualTo("30")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `Time extraction suffix format`() {
        val input = listOf("30 min de cuisson")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedCookTime).isEqualTo("30")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `Disqualification via instruction trigger`() {
        val input = listOf("Cuire pendant 10 min")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Doit être une instruction à cause du déclencheur 'pendant'")
            .that(result.rawInstructionsList).contains("Cuire pendant 10 min")
        assertWithMessage("Ne doit pas être un ingrédient")
            .that(result.rawIngredientsList).isEmpty()
    }
}
