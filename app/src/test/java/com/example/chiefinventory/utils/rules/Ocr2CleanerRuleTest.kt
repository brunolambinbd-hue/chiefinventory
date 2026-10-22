package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.Ocr2Cleaner
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant les règles de nettoyage et d'exclusion de [Ocr2Cleaner]
 * sur des scénarios de fiches de recettes réelles.
 */
class Ocr2CleanerRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie le filtrage des lignes vides ou constituées uniquement d'espaces et de saut de lignes.
     */
    @Test
    fun `clean should filter out empty or blank lines`() {
        val input = listOf("", "   ", "\t", "\n", "Sel")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList())
        assertThat(result).containsExactly("Sel")
    }

    /**
     * Vérifie le filtrage des mots-clés d'exclusion stricte issus du dictionnaire XML de production [baseVocabulary.excludedOcrKeywords].
     */
    @Test
    fun `clean should filter out strict excluded keywords using production XML`() {
        val input = listOf("Page", "Saveur", "Oignon")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = baseVocabulary.excludedOcrKeywords)
        assertThat(result).containsExactly("Oignon")
    }

    /**
     * Vérifie le filtrage des motifs publicitaires définis sous forme d'expressions régulières.
     */
    @Test
    fun `clean should filter out advertisements via regex`() {
        val adExclusions = listOf(".*promo.*", ".*pub.*")
        val input = listOf("Super promo ici", "100 g de beurre")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("100 g de beurre")
    }

    /**
     * Vérifie le filtrage des mentions de présentoirs de fiches recettes issus du dictionnaire de production [baseVocabulary.adExclusions].
     */
    @Test
    fun `clean should filter out presentation plate marketing captions via production ads_exclusions`() {
        val adExclusions = listOf(".*Assiette de pr[eéèê]sentation.*", ".*disponible dans les.*")
        val input = listOf(
            "Assiette de présentation disponible dans les Maxi GB",
            "Assiette de presentation disponible dans les Maxi GB",
            "100 g de beurre"
        )
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("100 g de beurre")
    }

    /**
     * Vérifie l'effacement du préfixe publicitaire lorsqu'il est accidentellement collé devant le texte d'une consigne d'instruction.
     */
    @Test
    fun `clean should strip marketing caption prefix from concatenated instruction line`() {
        val adExclusions = listOf("Assiette de pr[eéèê]sentation disponible dans les Maxi GB")
        val input = listOf(
            "Assiette de présentation disponible dans les Maxi GB Pendant ce temps, préparez la vinaigrette."
        )
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList(), adExclusions = adExclusions)
        assertThat(result).containsExactly("Pendant ce temps, préparez la vinaigrette.")
    }

    /**
     * Vérifie le filtrage des chiffres isolés jusqu'à 3 caractères (numéros de page bruts).
     */
    @Test
    fun `clean should filter out isolated digits up to 3 chars`() {
        val input = listOf("1", "12", "123", "2024", "Sel")
        val result = Ocr2Cleaner.clean(input, excludedKeywords = emptyList())
        assertThat(result).containsExactly("2024", "Sel").inOrder()
    }

    /**
     * Vérifie la détection des dimensions complexes avec connecteurs ("5 mm sur 5 mm", "10x10cm", "100 cm2 x 50 cm2").
     */
    @Test
    fun `isTechnicalDimension should identify dimensions with connectors`() {
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm sur 5 mm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("10x10cm")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("100 cm2 x 50 cm2")).isTrue()
        assertThat(Ocr2Cleaner.isTechnicalDimension("5 mm")).isFalse()
        assertThat(Ocr2Cleaner.isTechnicalDimension("250 g")).isFalse()
    }
}
