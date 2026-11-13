package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("La plage de poids entre parenthèses '(125 à 180 g)' doit être conservée dans la ligne restante")
            .that(res.remainingLine)
            .contains("(125 à 180 g)")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_OrphanedTimeHeadersWithoutDigits() {
        val results = RawSections()

        val res1 = RecipeMetadataParser.extractMetadata("Temps de préparation :", results)
        val res2 = RecipeMetadataParser.extractMetadata("Temps de cuisson", results)
        val res3 = RecipeMetadataParser.extractMetadata("Temps de", results)
        val res4 = RecipeMetadataParser.extractMetadata("Préparation :", results)

        assertWithMessage(" L'en-tête 'Temps de préparation :' doit être qualifié de méta-donnée pure")
            .that(res1.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Temps de préparation :' doit être vide")
            .that(res1.remainingLine)
            .isEmpty()

        assertWithMessage("L'en-tête 'Temps de cuisson' doit être qualifié de méta-donnée pure")
            .that(res2.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Temps de cuisson' doit être vide")
            .that(res2.remainingLine)
            .isEmpty()

        assertWithMessage("L'en-tête 'Temps de' doit être qualifié de méta-donnée pure")
            .that(res3.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Temps de' doit être vide")
            .that(res3.remainingLine)
            .isEmpty()

        assertWithMessage("L'en-tête 'Préparation :' doit être qualifié de méta-donnée pure")
            .that(res4.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Préparation :' doit être vide")
            .that(res4.remainingLine)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_CuissonAndReposOrphanHeaders() {
        val results1 = RawSections()
        val resCuisson = RecipeMetadataParser.extractMetadata("Cuisson :", results1)
        assertWithMessage("L'en-tête 'Cuisson :' doit être qualifié de méta-donnée pure")
            .that(resCuisson.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Cuisson :' doit être vide")
            .that(resCuisson.remainingLine)
            .isEmpty()

        val results2 = RawSections()
        val resRepos = RecipeMetadataParser.extractMetadata("Repos :", results2)
        assertWithMessage("L'en-tête 'Repos :' doit être qualifié de méta-donnée pure")
            .that(resRepos.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Repos :' doit être vide")
            .that(resRepos.remainingLine)
            .isEmpty()

        val results3 = RawSections()
        val resPour = RecipeMetadataParser.extractMetadata("Pour", results3)
        assertWithMessage("L'en-tête 'Pour' isolé doit être qualifié de méta-donnée pure")
            .that(resPour.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne restante pour 'Pour' doit être vide")
            .that(resPour.remainingLine)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_PriceAndBudgetRatings() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        val results1 = RawSections()
        val res1 = RecipeMetadataParser.extractMetadata("Prix : bon marché", results1, vocabulary)
        assertWithMessage("Le niveau de prix bas doit être extrait avec le code '1'")
            .that(results1.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_LOW)

        assertWithMessage("La ligne de prix 'Prix : bon marché' doit être qualifiée de méta-donnée pure")
            .that(res1.isPureMetadata)
            .isTrue()

        val results2 = RawSections()
        val res2 = RecipeMetadataParser.extractMetadata("Coût moyen", results2, vocabulary)
        assertWithMessage("Le niveau de prix moyen doit être extrait avec le code '2'")
            .that(results2.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_MEDIUM)

        assertWithMessage("La ligne 'Coût moyen' doit être qualifiée de méta-donnée pure")
            .that(res2.isPureMetadata)
            .isTrue()

        val results3 = RawSections()
        val res3 = RecipeMetadataParser.extractMetadata("Assez cher", results3, vocabulary)
        assertWithMessage("Le niveau de prix élevé doit être extrait avec le code '3'")
            .that(results3.detectedPriceRating)
            .isEqualTo(RecipeMetadataParser2_BlockB_Price.PRICE_HIGH)

        assertWithMessage("La ligne 'Assez cher' doit être qualifiée de méta-donnée pure")
            .that(res3.isPureMetadata)
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_PreserveTimeInsideInstructionSentence() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val results = RawSections()
        val res = RecipeMetadataParser.extractMetadata("Comptez 10 mn de cuisson.", results, vocabulary)

        assertWithMessage("Le temps de cuisson '10' doit être extrait")
            .that(results.detectedCookTime)
            .isEqualTo("10")

        assertWithMessage("La consigne de cuisson complète ne doit pas être qualifiée de méta-donnée pure")
            .that(res.isPureMetadata)
            .isFalse()

        assertWithMessage("La phrase de consigne complète doit être préservée dans la ligne restante")
            .that(res.remainingLine)
            .isEqualTo("Comptez 10 mn de cuisson.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_ComplexDurationsAndServingsAndKcalAndDifficulty() {
        val results1 = RawSections()
        RecipeMetadataParser.extractMetadata("Préparation : 1h15 - Cuisson : 45 min - Repos : 2 heures", results1)
        assertWithMessage("Le temps de préparation '1h15' doit être converti en 75 minutes")
            .that(results1.detectedPrepTime)
            .isEqualTo("75")

        assertWithMessage("Le temps de cuisson '45 min' doit être extrait")
            .that(results1.detectedCookTime)
            .isEqualTo("45")

        assertWithMessage("Le temps de repos '2 heures' doit être converti en 120 minutes")
            .that(results1.detectedRestingTime)
            .isEqualTo("120")

        val results2 = RawSections()
        RecipeMetadataParser.extractMetadata("Pour 4 à 6 personnes - 450 kcal/portion - Difficulté : facile", results2)
        assertWithMessage("Le nombre de portions '4' doit être extrait")
            .that(results2.detectedServings)
            .isEqualTo("4")

        assertWithMessage("Les kilocalories '450' doivent être extraites")
            .that(results2.detectedKcal)
            .isEqualTo("450")

        assertWithMessage("La difficulté 'facile' doit être extraite")
            .that(results2.detectedDifficulty)
            .isEqualTo("facile")

        val results3 = RawSections()
        RecipeMetadataParser.extractMetadata("10 personnes - 350 kcal - Diffcile", results3)
        assertWithMessage("Le nombre de portions '10' doit être extrait")
            .that(results3.detectedServings)
            .isEqualTo("10")

        assertWithMessage("Les kilocalories '350' doivent être extraites")
            .that(results3.detectedKcal)
            .isEqualTo("350")

        assertWithMessage("La faute 'Diffcile' doit être corrigée en 'difficile'")
            .that(results3.detectedDifficulty)
            .isEqualTo("difficile")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_OcrVariationsA4PersOr4Pers() {
        val results1 = RawSections()
        RecipeMetadataParser.extractMetadata("A4PERS", results1)
        assertWithMessage("La faute OCR 'A4PERS' doit extraire 4 portions")
            .that(results1.detectedServings)
            .isEqualTo("4")

        val results2 = RawSections()
        RecipeMetadataParser.extractMetadata("4 PERS.", results2)
        assertWithMessage("L'abréviation '4 PERS.' doit extraire 4 portions")
            .that(results2.detectedServings)
            .isEqualTo("4")

        val results3 = RawSections()
        RecipeMetadataParser.extractMetadata("A 6 PERS", results3)
        assertWithMessage("La variante OCR 'A 6 PERS' doit extraire 6 portions")
            .that(results3.detectedServings)
            .isEqualTo("6")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMetadata_NutritionInfoBlockPer100gAndSugarsWithoutCarbs() {
        val results = RawSections()

        val line1 = "VALEURS NUTRITIONNELLES PAR 100G"
        val line2 = "ÉNERGIE 646 KJ / 156 KCAL | LIPIDES 9.7 G | ACIDES GRAS SATURÉS 1.0 G | GLUCIDES 10.7 G | SUCRES 2.0 G | FIBRES 1.0 G | PROTÉINES 8.8 G | SEL 0.40 G"

        val res1 = RecipeMetadataParser.extractMetadata(line1, results)
        val res2 = RecipeMetadataParser.extractMetadata(line2, results)

        assertWithMessage("L'en-tête de valeurs nutritionnelles doit être qualifié de méta-donnée pure")
            .that(res1.isPureMetadata)
            .isTrue()

        assertWithMessage("La ligne de valeurs nutritionnelles doit être qualifiée de méta-donnée pure")
            .that(res2.isPureMetadata)
            .isTrue()

        assertWithMessage("Les kilocalories '156' doivent être extraites dans detectedKcal")
            .that(results.detectedKcal)
            .isEqualTo("156")

        assertWithMessage("L'information nutritionnelle doit contenir '156 kcal'")
            .that(results.detectedNutritionInfo)
            .contains("156 kcal")

        assertWithMessage("L'information nutritionnelle doit contenir 'Lipides: 9.7g'")
            .that(results.detectedNutritionInfo)
            .contains("Lipides: 9.7g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Glucides: 10.7g'")
            .that(results.detectedNutritionInfo)
            .contains("Glucides: 10.7g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Protéines: 8.8g'")
            .that(results.detectedNutritionInfo)
            .contains("Protéines: 8.8g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Sel: 0.40g'")
            .that(results.detectedNutritionInfo)
            .contains("Sel: 0.40g")

        // Test sugar match when carbs is null (sugar appended)
        val resultsSugars = RawSections()
        RecipeMetadataParser.extractMetadata("SUCRES 5.0 G | GLUCIDES 20.0 G", resultsSugars)
        assertWithMessage("Les glucides et sucres doivent être combinés 'Glucides: 20.0g (dont sucres: 5.0g)'")
            .that(resultsSugars.detectedNutritionInfo)
            .contains("Glucides: 20.0g (dont sucres: 5.0g)")
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

        assertWithMessage("Les kilocalories corrompues '198' doivent être extraites")
            .that(results.detectedKcal)
            .isEqualTo("198")

        assertWithMessage("L'information nutritionnelle doit contenir '198 kcal'")
            .that(results.detectedNutritionInfo)
            .contains("198 kcal")

        assertWithMessage("L'information nutritionnelle doit contenir 'Lipides: 9.7g (dont saturés: 1.0g)'")
            .that(results.detectedNutritionInfo)
            .contains("Lipides: 9.7g (dont saturés: 1.0g)")

        assertWithMessage("L'information nutritionnelle doit contenir 'Glucides: 10.7'")
            .that(results.detectedNutritionInfo)
            .contains("Glucides: 10.7")

        assertWithMessage("L'information nutritionnelle doit contenir 'dont sucres: 2.0g'")
            .that(results.detectedNutritionInfo)
            .contains("dont sucres: 2.0g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Protéines: 8.8g'")
            .that(results.detectedNutritionInfo)
            .contains("Protéines: 8.8g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Fibres: 1.0g'")
            .that(results.detectedNutritionInfo)
            .contains("Fibres: 1.0g")

        assertWithMessage("L'information nutritionnelle doit contenir 'Sel: 0.40g'")
            .that(results.detectedNutritionInfo)
            .contains("Sel: 0.40g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractMinutes_DurationConversions() {
        assertWithMessage("La durée '1 h 30 min' doit être convertie en 90 minutes")
            .that(RecipeMetadataParser.extractMinutes("1 h 30 min"))
            .isEqualTo("90")

        assertWithMessage("La durée '2h' doit être convertie en 120 minutes")
            .that(RecipeMetadataParser.extractMinutes("2h"))
            .isEqualTo("120")

        assertWithMessage("La durée '45 mn' doit extraire 45 minutes")
            .that(RecipeMetadataParser.extractMinutes("45 mn"))
            .isEqualTo("45")

        assertWithMessage("La durée '30 sec' doit extraire 30 secondes")
            .that(RecipeMetadataParser.extractMinutes("30 sec"))
            .isEqualTo("30")

        assertWithMessage("Le nombre '45' seul doit extraire 45 minutes")
            .that(RecipeMetadataParser.extractMinutes("45"))
            .isEqualTo("45")
    }
}
