package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr2Cleaner].
 */
class Ocr2CleanerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie la suppression du bruit de scan OCR et artefacts d'images issus de [OcrVocabulary.ocrNoiseExclusions] (`ocr_noise_exclusions.xml`).
     */
    @Test
    fun `clean should remove scan noise lines from ocr_noise_exclusions`() {
        val input = listOf(
            "DUITe TOugp MO 100s",
            "100 g de beurre",
            "EprrtEANS",
            "FOTIC"
        )

        val result = Ocr2Cleaner.clean(
            input,
            vocabulary.excludedOcrKeywords,
            vocabulary.adExclusions,
            vocabulary.ocrNoiseExclusions
        )

        assertThat(result).containsExactly(
            "100 g de beurre"
        )
    }

    /**
     * Vérifie la suppression des lignes de bruits publicitaires complets issues de [OcrVocabulary.adExclusions] (`ads_exclusions.xml`).
     */
    @Test
    fun `clean should remove full advertisement lines from ads_exclusions`() {
        val input = listOf(
            "1 avocat",
            "200 g de farine",
            "Assiette de présentation disponible dans les Maxi GB"
        )

        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords, vocabulary.adExclusions, vocabulary.ocrNoiseExclusions)

        assertThat(result).containsExactly(
            "1 avocat",
            "200 g de farine"
        )
    }

    /**
     * Vérifie la suppression des mots-clés d'exclusion stricte et des numéros de pages isolés issues de [OcrVocabulary.excludedOcrKeywords].
     */
    @Test
    fun `clean should remove strict excluded keywords like page numbers`() {
        val input = listOf(
            "100 g de beurre",
            "45",
            "PAGE"
        )

        val result = Ocr2Cleaner.clean(input, vocabulary.excludedOcrKeywords, vocabulary.adExclusions, vocabulary.ocrNoiseExclusions)

        assertThat(result).containsExactly(
            "100 g de beurre"
        )
    }

    /**
     * Vérifie le filtrage systématique d'un échantillon représentatif de mots exclus du fichier XML de production `ocr_step2_excluded_keywords.xml`.
     */
    @Test
    fun `clean should remove production strict excluded keywords sample`() {
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

        assertThat(result).containsExactly(
            "100 g de beurre"
        )
    }

    /**
     * Vérifie l'identification exhaustive des dimensions techniques (mm/cm avec connecteurs 'sur' ou 'x')
     * et leur rejet strict pour les ingrédients ou phrases de consignes.
     */
    @Test
    fun `isTechnicalDimension should exhaustively match dimensions and reject non-dimensions`() {
        val validDimensions = listOf(
            "5 mm sur 5 mm",
            "1 cm x 2 cm",
            "10x10cm",
            "100 cm2 x 50 cm2",
            "2,5 mm X 3 mm",
            "10 MM SUR 20 MM",
            "Moule de 20 cm x 30 cm"
        )
        validDimensions.forEach { line ->
            com.google.common.truth.Truth.assertWithMessage("Doit être reconnu comme dimension technique : '$line'")
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
            com.google.common.truth.Truth.assertWithMessage("Ne doit PAS être reconnu comme dimension technique : '$line'")
                .that(Ocr2Cleaner.isTechnicalDimension(line))
                .isFalse()
        }
    }
}
