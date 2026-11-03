package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [RecipeMetadataParser0_Nutrition].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecipeMetadataParser0_NutritionTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalizeNoisyNutritionalValue_AllCases() {
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("LO0")).isEqualTo("1.0")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("880")).isEqualTo("8.8")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("110")).isEqualTo("11.0")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("120")).isEqualTo("12.0")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("107")).isEqualTo("10.7")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("970")).isEqualTo("9.7")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("97")).isEqualTo("9.7")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("12")).isEqualTo("12")
        assertThat(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("10")).isEqualTo("10")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessNutritionBlock_WithFatSatCarbsSugarProteinsAndPer100g() {
        val results = RawSections()
        val line = "Valeurs nutritionnelles pour 100g : Lipides 12g dont saturés 3g Glucides 45g dont sucres 10g Protéines 8g Sel 1g Fibres 2g 250 kcal"
        val lower = line.lowercase()

        val res = RecipeMetadataParser0_Nutrition.processNutritionBlock(line, lower, results)

        assertThat(res).isNotNull()
        assertThat(res!!.isPureMetadata).isTrue()
        assertThat(results.detectedKcal).isEqualTo("250")
        assertThat(results.detectedNutritionInfo).contains("Lipides: 12g (dont saturés: 3g)")
        assertThat(results.detectedNutritionInfo).contains("Glucides: 4.5g (dont sucres: 10g)")
        assertThat(results.detectedNutritionInfo).contains("Protéines: 8g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessNutritionBlock_SaturatesAndSugarsStandaloneInExistingInfo() {
        // 1. With existing Lipides and Glucides
        val results = RawSections()
        results.detectedNutritionInfo = "Valeurs : Lipides: 10g, Glucides: 20g"

        val lineSat = "250 kcal dont saturés 2g dont sucres 5g"
        val lowerSat = lineSat.lowercase()

        val res = RecipeMetadataParser0_Nutrition.processNutritionBlock(lineSat, lowerSat, results)

        assertThat(res).isNotNull()
        assertThat(results.detectedNutritionInfo).contains("dont saturés: 2g")
        assertThat(results.detectedNutritionInfo).contains("dont sucres: 5g")

        // 2. Already contains saturates and sugars in existing info
        val resultsAlreadySat = RawSections()
        resultsAlreadySat.detectedNutritionInfo = "Valeurs : Lipides: 10g (dont saturés: 2g), Glucides: 20g (dont sucres: 5g)"
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont saturés 2g dont sucres 5g", "dont saturés 2g dont sucres 5g", resultsAlreadySat)
        assertThat(resultsAlreadySat.detectedNutritionInfo).contains("dont saturés: 2g")

        // 3. Standalone saturates without existing Lipides and per 1000 reference
        val resultsSatOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont saturés 2g pour 1000", "dont saturés 2g pour 1000", resultsSatOnly)
        assertThat(resultsSatOnly.detectedNutritionInfo).contains("Acides gras saturés: 2g")
        assertThat(resultsSatOnly.detectedNutritionInfo).contains("/100g")

        // 4. Standalone sugars without existing Glucides
        val resultsSugarOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont sucres 5g", "dont sucres 5g", resultsSugarOnly)
        assertThat(resultsSugarOnly.detectedNutritionInfo).contains("Sucres: 5g")

        // 5. Carbs without sugars
        val resultsCarbsOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Glucides 45g", "glucides 45g", resultsCarbsOnly)
        assertThat(resultsCarbsOnly.detectedNutritionInfo).contains("Glucides: 4.5g")

        // 6. Already has detectedKcal
        val resultsAlreadyKcal = RawSections()
        resultsAlreadyKcal.detectedKcal = "300"
        RecipeMetadataParser0_Nutrition.processNutritionBlock("250 kcal Lipides 10g", "250 kcal lipides 10g", resultsAlreadyKcal)
        assertThat(resultsAlreadyKcal.detectedKcal).isEqualTo("300")

        // 7. Reference "100 g" (with space)
        val results100gSpace = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Lipides 10g pour 100 g", "lipides 10g pour 100 g", results100gSpace)
        assertThat(results100gSpace.detectedNutritionInfo).contains("/100g")

        // 8. Salt only without fat, carbs or protein
        val resultsSaltOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Sel 1g", "sel 1g", resultsSaltOnly)
        assertThat(resultsSaltOnly.detectedNutritionInfo).contains("Sel: 1g")
    }
}
