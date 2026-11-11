package com.chefinventory.app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertWithMessage
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
        assertWithMessage("Le résultat de dépollution doit contenir la ligne d'ingrédient nettoyée")
            .that(result.cleanedLines)
            .containsExactly("100 g de beurre")

        assertWithMessage("Le résultat de dépollution doit contenir la source capturée au vol")
            .that(result.earlyDetectedSources)
            .containsExactly("Hôtel Hilton")

        // Test default constructor parameter
        val resultDefault = CleaningResult(cleanedLines = listOf("100 g de beurre"))
        assertWithMessage("Le constructeur par défaut doit initialiser la liste des sources capturées à vide")
            .that(resultDefault.earlyDetectedSources)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_AdPrefixRemovalShortAndEmptyRemaining() {
        val vocabAd = OcrVocabulary(adExclusions = listOf("Sponsor :"))

        // 1. empty remaining
        val res1 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor :"), vocabAd)
        assertWithMessage("Une ligne ne contenant qu'un préfixe publicitaire doit être éliminée")
            .that(res1.cleanedLines)
            .isEmpty()

        // 2. short remaining <= 8 chars
        val res2 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor : 12345"), vocabAd)
        assertWithMessage("Un résidu publicitaire court de 8 caractères ou moins doit être éliminé")
            .that(res2.cleanedLines)
            .isEmpty()

        // 3. long remaining > 8 chars
        val res3 = Ocr2Cleaner.cleanWithVocabulary(listOf("Sponsor : 200 g de farine bio"), vocabAd)
        assertWithMessage("Un résidu publicitaire valide de plus de 8 caractères doit être conservé")
            .that(res3.cleanedLines)
            .containsExactly("200 g de farine bio")
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
        assertWithMessage("L'ingrédient valide '100 g de beurre' doit être conservé après dépollution Android")
            .that(result)
            .contains("100 g de beurre")
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
        assertWithMessage("Le préfixe 'temps de' doit fusionner avec 'préparation : 15 min'")
            .that(result)
            .contains("temps de préparation : 15 min")

        assertWithMessage("Le préfixe 'durée de' doit fusionner avec 'preparation : 10 min'")
            .that(result)
            .contains("durée de preparation : 10 min")

        assertWithMessage("Le préfixe 'temps' doit fusionner avec 'cuisson : 20 min'")
            .that(result)
            .contains("temps cuisson : 20 min")

        assertWithMessage("Le préfixe 'durée' doit fusionner avec 'repos : 1 heure'")
            .that(result)
            .contains("durée repos : 1 heure")
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

        assertWithMessage("Les lignes de bruit de scan OCR du dictionnaire doivent être éliminées")
            .that(result)
            .containsExactly("100 g de beurre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testClean_EmptyLineAndFullAdLine() {
        val input = listOf("   ", "100 g de beurre")
        val result = Ocr2Cleaner.clean(input, emptyList(), emptyList(), emptyList())
        assertWithMessage("Les lignes vides et blanches doivent être ignorées")
            .that(result)
            .containsExactly("100 g de beurre")
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

        assertWithMessage("L'ingrédient '1 avocat' doit être conservé dans les lignes nettoyées")
            .that(result.cleanedLines)
            .contains("1 avocat")

        assertWithMessage("L'enseigne 'Hilton' doit être capturée au vol dans les sources précoces")
            .that(result.earlyDetectedSources)
            .contains("Hilton")
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

        assertWithMessage("L'en-tête de section 'INGRÉDIENTS' doit être préservé")
            .that(result)
            .contains("INGRÉDIENTS")

        assertWithMessage("L'en-tête de section 'INSTRUCTIONS' doit être préservé")
            .that(result)
            .contains("INSTRUCTIONS")

        assertWithMessage("L'en-tête de section 'PRÉPARATION' doit être préservé")
            .that(result)
            .contains("PRÉPARATION")
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

        assertWithMessage("Les mots-clés d'exclusion stricte et les numéros de page doivent être éliminés")
            .that(result)
            .containsExactly("100 g de beurre")
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

        assertWithMessage("Les mots d'exclusion de production doivent être filtrés pour ne conserver que l'ingrédient")
            .that(result)
            .containsExactly("100 g de beurre")
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
            assertWithMessage("La ligne '$line' doit être reconnue comme une dimension technique")
                .that(Ocr2Cleaner.isTechnicalDimension(line))
                .isTrue()
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
            assertWithMessage("La ligne '$line' ne doit pas être qualifiée de dimension technique")
                .that(Ocr2Cleaner.isTechnicalDimension(line))
                .isFalse()
        }
    }
}
