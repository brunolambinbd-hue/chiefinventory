package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Le bruit OCR 'LO0' doit être normalisé en '1.0'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("LO0"))
            .isEqualTo("1.0")

        assertWithMessage("La valeur '880' sans point doit être normalisée en '8.8'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("880"))
            .isEqualTo("8.8")

        assertWithMessage("La valeur '110' doit être normalisée en '11.0'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("110"))
            .isEqualTo("11.0")

        assertWithMessage("La valeur '120' doit être normalisée en '12.0'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("120"))
            .isEqualTo("12.0")

        assertWithMessage("La valeur '107' doit être normalisée en '10.7'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("107"))
            .isEqualTo("10.7")

        assertWithMessage("La valeur '970' doit être normalisée en '9.7'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("970"))
            .isEqualTo("9.7")

        assertWithMessage("La valeur '97' doit être normalisée en '9.7'")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("97"))
            .isEqualTo("9.7")

        assertWithMessage("La valeur '12' doit être conservée")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("12"))
            .isEqualTo("12")

        assertWithMessage("La valeur '10' doit être conservée")
            .that(RecipeMetadataParser0_Nutrition.normalizeNoisyNutritionalValue("10"))
            .isEqualTo("10")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessNutritionBlock_WithFatSatCarbsSugarProteinsAndPer100g() {
        val results = RawSections()
        val line = "Valeurs nutritionnelles pour 100g : Lipides 12g dont saturés 3g Glucides 45g dont sucres 10g Protéines 8g Sel 1g Fibres 2g 250 kcal"
        val lower = line.lowercase()

        val res = RecipeMetadataParser0_Nutrition.processNutritionBlock(line, lower, results)

        assertWithMessage("Le résultat du traitement du bloc nutritionnel ne doit pas être null")
            .that(res)
            .isNotNull()

        assertWithMessage("Le bloc nutritionnel complet doit être qualifié de méta-données pures")
            .that(res!!.isPureMetadata)
            .isTrue()

        assertWithMessage("Les kilocalories '250' doivent être extraites dans detectedKcal")
            .that(results.detectedKcal)
            .isEqualTo("250")

        assertWithMessage("L'information nutritionnelle extraite doit contenir Lipides et saturés")
            .that(results.detectedNutritionInfo)
            .contains("Lipides: 12g (dont saturés: 3g)")

        assertWithMessage("L'information nutritionnelle extraite doit contenir Glucides et sucres")
            .that(results.detectedNutritionInfo)
            .contains("Glucides: 4.5g (dont sucres: 10g)")

        assertWithMessage("L'information nutritionnelle extraite doit contenir Protéines: 8g")
            .that(results.detectedNutritionInfo)
            .contains("Protéines: 8g")
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

        assertWithMessage("Le traitement des saturés/sucres autonomes ne doit pas être null")
            .that(res)
            .isNotNull()

        assertWithMessage("Les acides gras saturés doivent être ajoutés à l'information existante")
            .that(results.detectedNutritionInfo)
            .contains("dont saturés: 2g")

        assertWithMessage("Les sucres doivent être ajoutés à l'information existante")
            .that(results.detectedNutritionInfo)
            .contains("dont sucres: 5g")

        // 2. Already contains saturates and sugars in existing info
        val resultsAlreadySat = RawSections()
        resultsAlreadySat.detectedNutritionInfo = "Valeurs : Lipides: 10g (dont saturés: 2g), Glucides: 20g (dont sucres: 5g)"
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont saturés 2g dont sucres 5g", "dont saturés 2g dont sucres 5g", resultsAlreadySat)
        assertWithMessage("L'information nutritionnelle déjà présente doit conserver les saturés")
            .that(resultsAlreadySat.detectedNutritionInfo)
            .contains("dont saturés: 2g")

        // 3. Standalone saturates without existing Lipides and per 1000 reference
        val resultsSatOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont saturés 2g pour 1000", "dont saturés 2g pour 1000", resultsSatOnly)
        assertWithMessage("Les saturés autonomes doivent être nommés 'Acides gras saturés: 2g'")
            .that(resultsSatOnly.detectedNutritionInfo)
            .contains("Acides gras saturés: 2g")

        assertWithMessage("La référence pour 1000 doit être convertie en /100g")
            .that(resultsSatOnly.detectedNutritionInfo)
            .contains("/100g")

        // 4. Standalone sugars without existing Glucides
        val resultsSugarOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("dont sucres 5g", "dont sucres 5g", resultsSugarOnly)
        assertWithMessage("Les sucres autonomes doivent être nommés 'Sucres: 5g'")
            .that(resultsSugarOnly.detectedNutritionInfo)
            .contains("Sucres: 5g")

        // 5. Carbs without sugars
        val resultsCarbsOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Glucides 45g", "glucides 45g", resultsCarbsOnly)
        assertWithMessage("Les glucides autonomes '45g' doivent être normalisés en '4.5g'")
            .that(resultsCarbsOnly.detectedNutritionInfo)
            .contains("Glucides: 4.5g")

        // 6. Already has detectedKcal
        val resultsAlreadyKcal = RawSections()
        resultsAlreadyKcal.detectedKcal = "300"
        RecipeMetadataParser0_Nutrition.processNutritionBlock("250 kcal Lipides 10g", "250 kcal lipides 10g", resultsAlreadyKcal)
        assertWithMessage("Des kilocalories déjà détectées '300' doivent être conservées")
            .that(resultsAlreadyKcal.detectedKcal)
            .isEqualTo("300")

        // 7. Reference "100 g" (with space)
        val results100gSpace = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Lipides 10g pour 100 g", "lipides 10g pour 100 g", results100gSpace)
        assertWithMessage("La référence 'pour 100 g' avec espace doit être normalisée en /100g")
            .that(results100gSpace.detectedNutritionInfo)
            .contains("/100g")

        // 8. Salt only without fat, carbs or protein
        val resultsSaltOnly = RawSections()
        RecipeMetadataParser0_Nutrition.processNutritionBlock("Sel 1g", "sel 1g", resultsSaltOnly)
        assertWithMessage("Le sel seul doit être extrait comme 'Sel: 1g'")
            .that(resultsSaltOnly.detectedNutritionInfo)
            .contains("Sel: 1g")
    }
}
