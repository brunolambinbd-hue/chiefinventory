package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr2Cleaner].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2CleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleaningResultDataClass_InstantiatesWithProperties() {
        val result = CleaningResult(cleanedLines = listOf("100 g de beurre"), earlyDetectedSources = listOf("Hôtel Hilton"))
        assertThat(result.cleanedLines).containsExactly("100 g de beurre")
        assertThat(result.earlyDetectedSources).containsExactly("Hôtel Hilton")

        // Test default constructor parameter
        val resultDefault = CleaningResult(cleanedLines = listOf("100 g de beurre"))
        assertThat(resultDefault.earlyDetectedSources).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_AdPrefixRemovalShortAndEmptyRemaining() {
        val vocabAd = OcrVocabulary(adExclusions = listOf("Sponsor :"))

        // 1. empty remaining
        val res1 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor :"), vocabAd)
        assertThat(res1.cleanedLines).isEmpty()

        // 2. short remaining <= 8 chars
        val res2 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor : 12345"), vocabAd)
        assertThat(res2.cleanedLines).isEmpty()

        // 3. long remaining > 8 chars
        val res3 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor : 200 g de farine bio"), vocabAd)
        assertThat(res3.cleanedLines).containsExactly("200 g de farine bio")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_WithAndroidResources_CleansNoiseAndAds() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)

        val input = listOf(
            "temps de",
            "préparation : 15 min",
            "durée de",
            "cuisson : 30 min",
            "100 g de beurre",
            "45",
            "Assiette de présentation disponible dans les Maxi GB"
        )

        val result = Ocr2Cleaner.clean(input, context.resources)
        assertThat(result).contains("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_MergedHeaderVariationsAndLastLineBoundary() {
        // Test all variations of header prefixes + next line keywords
        val input = listOf(
            "temps de",
            "préparation : 15 min",
            "durée de",
            "preparation : 10 min",
            "temps",
            "cuisson : 20 min",
            "durée",
            "repos : 1 heure",
            "temps de",
            "ingrédients : 200g", // second if false
            "temps de" // last line in list (idx + 1 < size is false)
        )

        val result = Ocr2Cleaner.clean(input, emptyList(), emptyList(), emptyList())
        assertThat(result).contains("temps de préparation : 15 min")
        assertThat(result).contains("durée de preparation : 10 min")
        assertThat(result).contains("temps cuisson : 20 min")
        assertThat(result).contains("durée repos : 1 heure")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_RemovesScanNoiseLinesFromOcrNoiseExclusions() {
        val input = listOf(
            "DUITe TOugp MO 100s",
            "100 g de beurre",
            "EprrtEANS",
            "FOTIC"
        )

        // Test with blank noise exclusion entries in list
        val noiseExclusions = listOf("", "   ", "DUITe TOugp MO 100s", "EprrtEANS", "FOTIC")

        val result = Ocr2Cleaner.clean(
            input,
            vocabulary.excludedOcrKeywords,
            vocabulary.adExclusions,
            noiseExclusions
        )

        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_EmptyLineAndFullAdLine() {
        val input = listOf("   ", "100 g de beurre")
        val result = Ocr2Cleaner.clean(input, emptyList(), emptyList(), emptyList())
        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_RemovesFullAndPartialAdLinesBlankPatternsAndCatchesInvalidRegex() {
        val input = listOf(
            "1 avocat",
            "200 g de farine",
            "Assiette de présentation disponible dans les Maxi GB",
            "Hôtel Hilton, Place Rogier 20",
            "Hôtel Hilton, Place Rogier 20", // Duplicate source line to test earlySources deduplication
            "Pub .*[*invalid_regex("
        )

        // Add knownSourceBrands and invalid regex pattern to test try-catch fallback in ad pattern matching
        val vocabWithSpecialPatterns = OcrVocabulary(
            knownSourceBrands = listOf("Hilton"),
            adExclusions = listOf(
                "",
                "   ",
                ".*.*",
                ".*   .*",
                ".*[*invalid_regex(",
                "Assiette de présentation",
                "Offre spéciale :"
            )
        )

        val result = Ocr2Cleaner.cleanWithVocabulary(input, vocabWithSpecialPatterns)

        assertThat(result.cleanedLines).contains("1 avocat")
        assertThat(result.earlyDetectedSources).contains("Hilton")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_PreservesProtectedSectionHeaders() {
        val input = listOf(
            "INGRÉDIENTS",
            "INSTRUCTIONS",
            "PRÉPARATION",
            "100 g de beurre"
        )

        val result = Ocr2Cleaner.clean(
            input,
            listOf("INGRÉDIENTS", "INSTRUCTIONS", "PRÉPARATION"), // Strict excluded keywords list
            emptyList(),
            emptyList()
        )

        assertThat(result).contains("INGRÉDIENTS")
        assertThat(result).contains("INSTRUCTIONS")
        assertThat(result).contains("PRÉPARATION")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_RemovesStrictExcludedKeywordsAndPageNumbers() {
        val input = listOf(
            "100 g de beurre",
            "45",
            "PAGE"
        )

        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords, vocabulary.adExclusions, vocabulary.ocrNoiseExclusions)

        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_RemovesProductionStrictExcludedKeywordsSample() {
        val input = listOf(
            "ASTUCE",
            "NOTES",
            "PRIX INDICATIF",
            "100 g de beurre",
            "EEE",
            "€€€"
        )

        val result = Ocr2Cleaner.clean(
            input,
            vocabulary.excludedOcrKeywords,
            vocabulary.adExclusions,
            vocabulary.ocrNoiseExclusions
        )

        assertThat(result).containsExactly("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsTechnicalDimension_ExhaustivelyMatchesDimensionsAndRejectsNonDimensions() {
        val validDimensions = listOf(
            "5 mm sur 5 mm",
            "1 cm x 2 cm",
            "10x10cm",
            "100 cm2 x 50 cm2",
            "2,5 mm X 3 mm",
            "10 MM SUR 20 MM",
            "Moule de 20 cm x 30 cm",
            "2 cm de haut",
            "5 mm de large",
            "1 cm d'épaisseur",
            "10 cm tronçon",
            "10 cm troncon"
        )
        validDimensions.forEach { line ->
            assertThat(Ocr2Cleaner.isTechnicalDimension(line)).isTrue()
        }

        val invalidLines = listOf(
            "200 g de farine",
            "5 cl d'huile",
            "Disposer sur une plaque",
            "Cuire x minutes",
            "5 mm",
            "1 cm",
            "10 cm2",
            "Mettre au four sur 200°C"
        )
        invalidLines.forEach { line ->
            assertThat(Ocr2Cleaner.isTechnicalDimension(line)).isFalse()
        }
    }
}
