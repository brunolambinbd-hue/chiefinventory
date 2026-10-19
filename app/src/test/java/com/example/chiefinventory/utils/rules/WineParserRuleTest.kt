package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.WineParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WineParserRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `isWineLine should exclude line with culinary ingredients or units`() {
        assertThat(WineParser.isWineLine("Un bon vinaigre de vin 2018", baseVocabulary)).isFalse()
        assertThat(WineParser.isWineLine("Chardonnay avec 50 g de beurre", baseVocabulary)).isFalse()
    }

    @Test
    fun `isWineLine should reject garbled ocr artifacts`() {
        assertThat(WineParser.isWineLine("TOCRAINE", baseVocabulary)).isFalse()
        assertThat(WineParser.isWineLine("SAETICsox", baseVocabulary)).isFalse()
        assertThat(WineParser.isWineLine("AUVICNoN", baseVocabulary)).isFalse()
    }

    @Test
    fun `isWineLine should detect valid year and volume`() {
        assertThat(WineParser.isWineLine("Bouteille de 2015, 75cl", baseVocabulary)).isTrue()
        assertThat(WineParser.isWineLine("Format 750ml de 2020", baseVocabulary)).isTrue()
    }

    @Test
    fun `isWineLine should validate year format boundaries with neutral strings`() {
        assertThat(WineParser.isWineLine("Produit de 1899, 500ml", baseVocabulary)).isFalse()
        assertThat(WineParser.isWineLine("Produit de 2101, 500ml", baseVocabulary)).isFalse()
        assertThat(WineParser.isWineLine("Produit de 1999, 500ml", baseVocabulary)).isTrue()
    }

    @Test
    fun `isWineLine should match known appellations or producers from production XML`() {
        assertThat(WineParser.isWineLine("Un verre de Saumur", baseVocabulary)).isTrue()
        assertThat(WineParser.isWineLine("Cuvée Alban de Saint-Pé", baseVocabulary)).isTrue()
    }

    @Test
    fun `isWineLine should match custom synthetic pattern complement`() {
        val customVocabulary = baseVocabulary.copy(
            wineAppellations = baseVocabulary.wineAppellations + "nouveau_cepage_test"
        )
        assertThat(WineParser.isWineLine("Cuvée nouveau_cepage_test", customVocabulary)).isTrue()
    }

    @Test
    fun `cleanWineLine should clean removePattern and spelling errors`() {
        val cleaned = WineParser.cleanWineLine("Accord: Un grand Bordeau Atinum", baseVocabulary)
        assertThat(cleaned).isEqualTo("Un grand Bordeaux Atinium")
    }
}
