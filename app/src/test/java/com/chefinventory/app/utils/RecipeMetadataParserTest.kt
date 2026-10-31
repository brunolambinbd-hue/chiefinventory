package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction et la conversion des métadonnées de recettes par [RecipeMetadataParser].
 */
class RecipeMetadataParserTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_ParentheticalWeightRange() {
        val results = RawSections()
        val res = RecipeMetadataParser.extractMetadata("2 ou 3 paquets de jambon de Parme en fines tranches (125 à 180 g)", results)
        assertThat(res.remainingLine).contains("(125 à 180 g)")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_OrphanedTimeHeadersWithoutDigits() {
        val results = RawSections()

        val res1 = RecipeMetadataParser.extractMetadata("Temps de préparation :", results)
        val res2 = RecipeMetadataParser.extractMetadata("Temps de cuisson", results)
        val res3 = RecipeMetadataParser.extractMetadata("Temps de", results)
        val res4 = RecipeMetadataParser.extractMetadata("Préparation :", results)

        assertThat(res1.isPureMetadata).isTrue()
        assertThat(res1.remainingLine).isEmpty()

        assertThat(res2.isPureMetadata).isTrue()
        assertThat(res2.remainingLine).isEmpty()

        assertThat(res3.isPureMetadata).isTrue()
        assertThat(res3.remainingLine).isEmpty()

        assertThat(res4.isPureMetadata).isTrue()
        assertThat(res4.remainingLine).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_PriceAndBudgetRatings() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val results1 = RawSections()
        val res1 = RecipeMetadataParser.extractMetadata("Prix : bon marché", results1, vocabulary)
        assertThat(results1.detectedPriceRating).isEqualTo("Bon marché")
        assertThat(res1.isPureMetadata).isTrue()

        val results2 = RawSections()
        val res2 = RecipeMetadataParser.extractMetadata("Coût moyen", results2, vocabulary)
        assertThat(results2.detectedPriceRating).isEqualTo("Coût moyen")
        assertThat(res2.isPureMetadata).isTrue()

        val results3 = RawSections()
        val res3 = RecipeMetadataParser.extractMetadata("Assez cher", results3, vocabulary)
        assertThat(results3.detectedPriceRating).isEqualTo("Assez cher")
        assertThat(res3.isPureMetadata).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_PreserveTimeInsideInstructionSentence() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val results = RawSections()
        val res = RecipeMetadataParser.extractMetadata("Comptez 10 mn de cuisson.", results, vocabulary)

        assertThat(results.detectedCookTime).isEqualTo("10")
        assertThat(res.isPureMetadata).isFalse()
        assertThat(res.remainingLine).isEqualTo("Comptez 10 mn de cuisson.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_ComplexDurationsAndServingsAndKcalAndDifficulty() {
        val results1 = RawSections()
        RecipeMetadataParser.extractMetadata("Préparation : 1h15 - Cuisson : 45 min - Repos : 2 heures", results1)
        assertThat(results1.detectedPrepTime).isEqualTo("75")
        assertThat(results1.detectedCookTime).isEqualTo("45")
        assertThat(results1.detectedRestingTime).isEqualTo("120")

        val results2 = RawSections()
        RecipeMetadataParser.extractMetadata("Pour 4 à 6 personnes - 450 kcal/portion - Difficulté : facile", results2)
        assertThat(results2.detectedServings).isEqualTo("4")
        assertThat(results2.detectedKcal).isEqualTo("450")
        assertThat(results2.detectedDifficulty).isEqualTo("facile")

        val results3 = RawSections()
        RecipeMetadataParser.extractMetadata("10 personnes - 350 kcal - Diffcile", results3)
        assertThat(results3.detectedServings).isEqualTo("10")
        assertThat(results3.detectedKcal).isEqualTo("350")
        assertThat(results3.detectedDifficulty).isEqualTo("difficile")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_OcrVariationsA4PersOr4Pers() {
        val results1 = RawSections()
        RecipeMetadataParser.extractMetadata("A4PERS", results1)
        assertThat(results1.detectedServings).isEqualTo("4")

        val results2 = RawSections()
        RecipeMetadataParser.extractMetadata("4 PERS.", results2)
        assertThat(results2.detectedServings).isEqualTo("4")

        val results3 = RawSections()
        RecipeMetadataParser.extractMetadata("A 6 PERS", results3)
        assertThat(results3.detectedServings).isEqualTo("6")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_NutritionInfoBlockPer100gAndSugarsWithoutCarbs() {
        val results = RawSections()

        val line1 = "VALEURS NUTRITIONNELLES PAR 100G"
        val line2 = "ÉNERGIE 646 KJ / 156 KCAL | LIPIDES 9.7 G | ACIDES GRAS SATURÉS 1.0 G | GLUCIDES 10.7 G | SUCRES 2.0 G | FIBRES 1.0 G | PROTÉINES 8.8 G | SEL 0.40 G"

        val res1 = RecipeMetadataParser.extractMetadata(line1, results)
        val res2 = RecipeMetadataParser.extractMetadata(line2, results)

        assertThat(res1.isPureMetadata).isTrue()
        assertThat(res2.isPureMetadata).isTrue()

        assertThat(results.detectedKcal).isEqualTo("156")
        assertThat(results.detectedNutritionInfo).contains("156 kcal")
        assertThat(results.detectedNutritionInfo).contains("Lipides: 9.7g")
        assertThat(results.detectedNutritionInfo).contains("Glucides: 10.7g")
        assertThat(results.detectedNutritionInfo).contains("Protéines: 8.8g")
        assertThat(results.detectedNutritionInfo).contains("Sel: 0.40g")

        // Test sugar match when carbs is null (sugar appended)
        val resultsSugars = RawSections()
        RecipeMetadataParser.extractMetadata("SUCRES 5.0 G | GLUCIDES 20.0 G", resultsSugars)
        assertThat(resultsSugars.detectedNutritionInfo).contains("Glucides: 20.0g (dont sucres: 5.0g)")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_CorruptedNoisyOcrNutritionBlock() {
        val results = RawSections()

        val lines = listOf(
            "VALEURS NUTRITIONNELLES PAR 1000",
            "(NE ROIL 646 K/ 198 NCAL 1 IPOUS 97 0",
            "ACIOUS GRAS SATURES L00",
            "QLUCIDES 10,70| SUCRIS20",
            "toRES LO0 PROTCINES 880 1 SL0,40"
        )

        lines.forEach { line ->
            RecipeMetadataParser.extractMetadata(line, results)
        }

        assertThat(results.detectedKcal).isEqualTo("198")
        assertThat(results.detectedNutritionInfo).contains("198 kcal")
        assertThat(results.detectedNutritionInfo).contains("Lipides: 9.7g (dont saturés: 1.0g)")
        assertThat(results.detectedNutritionInfo).contains("Glucides: 10.7")
        assertThat(results.detectedNutritionInfo).contains("dont sucres: 2.0g")
        assertThat(results.detectedNutritionInfo).contains("Protéines: 8.8g")
        assertThat(results.detectedNutritionInfo).contains("Fibres: 1.0g")
        assertThat(results.detectedNutritionInfo).contains("Sel: 0.40g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMinutes_DurationConversions() {
        assertThat(RecipeMetadataParser.extractMinutes("1 h 30 min")).isEqualTo("90")
        assertThat(RecipeMetadataParser.extractMinutes("2h")).isEqualTo("120")
        assertThat(RecipeMetadataParser.extractMinutes("45 mn")).isEqualTo("45")
        assertThat(RecipeMetadataParser.extractMinutes("30 sec")).isEqualTo("30")
        assertThat(RecipeMetadataParser.extractMinutes("45")).isEqualTo("45")
    }
}
