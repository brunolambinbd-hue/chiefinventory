package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction et la conversion des métadonnées de recettes par [RecipeMetadataParser].
 */
class RecipeMetadataParserTest {

    @Test
    fun `extractMetadata should parse servings prep time cook time and kcal`() {
        val results = RawSections()
        
        val res1 = RecipeMetadataParser.extractMetadata("ingrédients pour 4 personnes", results)
        val res2 = RecipeMetadataParser.extractMetadata("préparation : 15 min", results)
        RecipeMetadataParser.extractMetadata("cuisson : 45 min", results)
        RecipeMetadataParser.extractMetadata("repos : 30 min", results)
        RecipeMetadataParser.extractMetadata("250 kcal", results)
        RecipeMetadataParser.extractMetadata("difficulté : facile", results)

        assertThat(results.detectedServings).isEqualTo("4")
        assertThat(results.detectedPrepTime).isEqualTo("15")
        assertThat(results.detectedCookTime).isEqualTo("45")
        assertThat(results.detectedRestingTime).isEqualTo("30")
        assertThat(results.detectedKcal).isEqualTo("250")
        assertThat(results.detectedDifficulty).isEqualTo("facile")

        assertThat(res1.remainingLine).isEqualTo("ingrédients")
        assertThat(res1.isPureMetadata).isFalse()
        assertThat(res2.isPureMetadata).isTrue()
    }

    @Test
    fun `extractMetadata should eliminate orphaned time headers without digits`() {
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
    fun `debug user canapes grecs scan`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val rawOcr = """
            Mélangez l'huile d'olive, le piment taillé en fines rondelles, le thym et le romarin effeuillé, une pin-
            cée de sel et placez-y le fromage découpé en tranches. Laissez mariner au réfrigérateur + 1/2 heures.
            Découpez la baguette en tranches d'1 cm, faites-les légèrement griller puis frottez-les
            d'ail, égouttez le fromage et placez-le sur le pain.
            Décorez d'olives coupées en deux et de romarin.
            ingrédients pour 4 personnes
            180 g de feta
            1/2 verre d'huile d'olive
            10 olives vertes dénoyautées
            10 olives noires dénoyautées
            1 petit piment
            1 gousse d'ail
            1 baguette de pain gris
            du thym et du romarin frais
            sel et poivre du moulin
        """.trimIndent()

        val result = RecipeOcrParser.parse(rawOcr, vocabulary)

        assertThat(result.servings).isEqualTo("4")
        assertThat(result.ingredients).contains("Du thym et du romarin frais")
        assertThat(result.ingredients).contains("Sel et poivre du moulin")
    }

    @Test
    fun `extractMetadata should handle OCR variations like A4PERS or 4 PERS`() {
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
    fun `extractMetadata should parse nutrition info block per 100g`() {
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
    }

    @Test
    fun `extractMetadata should handle heavily corrupted noisy OCR nutrition block`() {
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
    fun `extractMinutes should convert hours and minutes to total minutes`() {
        assertThat(RecipeMetadataParser.extractMinutes("1 h 30 min")).isEqualTo("90")
        assertThat(RecipeMetadataParser.extractMinutes("2h")).isEqualTo("120")
        assertThat(RecipeMetadataParser.extractMinutes("45 mn")).isEqualTo("45")
    }
}
