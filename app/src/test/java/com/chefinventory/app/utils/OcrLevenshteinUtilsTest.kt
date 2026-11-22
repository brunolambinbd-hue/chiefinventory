package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant le fonctionnement du composant [OcrLevenshteinUtils].
 */
class OcrLevenshteinUtilsTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDistance_CalculatesExactEditDistance() {
        assertThat(OcrLevenshteinUtils.distance("tomate", "tomate")).isEqualTo(0)
        assertThat(OcrLevenshteinUtils.distance("courgette", "courjete")).isEqualTo(2)
        assertThat(OcrLevenshteinUtils.distance("", "poireau")).isEqualTo(7)
        assertThat(OcrLevenshteinUtils.distance("oignon", "")).isEqualTo(6)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSimilarity_IgnoresAccentsAndCaseCorrectly() {
        val sim1 = OcrLevenshteinUtils.similarity("épinard", "epinard")
        assertWithMessage("'épinard' et 'epinard' doivent être identiques à 100% après retrait des accents")
            .that(sim1)
            .isEqualTo(1.0)

        val sim2 = OcrLevenshteinUtils.similarity("courgette", "courjete")
        assertWithMessage("'courgette' et 'courjete' doivent avoir une similarité élevée (> 0.75)")
            .that(sim2)
            .isGreaterThan(0.75)

        val sim3 = OcrLevenshteinUtils.similarity("tomate", "carotte")
        assertWithMessage("'tomate' et 'carotte' doivent avoir une similarité faible (< 0.50)")
            .that(sim3)
            .isLessThan(0.50)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindBestMatch_FindsMostSimilarIngredientFromDictionary() {
        val dictionary = listOf("Courgette", "Épinards", "Tomates", "Poireau", "Oignons")

        val match1 = OcrLevenshteinUtils.findBestMatch("courjete", dictionary)
        assertThat(match1).isEqualTo("Courgette")

        val match2 = OcrLevenshteinUtils.findBestMatch("epinard", dictionary)
        assertThat(match2).isEqualTo("Épinards")

        val match3 = OcrLevenshteinUtils.findBestMatch("oignon", dictionary)
        assertThat(match3).isEqualTo("Oignons")

        val match4 = OcrLevenshteinUtils.findBestMatch("chocolat", dictionary, minSimilarityThreshold = 0.75)
        assertThat(match4).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testGetMaxAllowedErrors_ProtectsShortWordsAndCorrectsLongWords() {
        // Mots courts (<= 3 lettres) -> 0 erreur
        assertThat(OcrLevenshteinUtils.getMaxAllowedErrors(3)).isEqualTo(0)
        assertThat(OcrLevenshteinUtils.isFuzzyMatch("Sel", "Gel")).isFalse()

        // Mots moyens (<= 5 lettres) -> 1 erreur max
        assertThat(OcrLevenshteinUtils.getMaxAllowedErrors(5)).isEqualTo(1)
        assertThat(OcrLevenshteinUtils.isFuzzyMatch("thon", "thym")).isFalse()

        // Mots longs (<= 9 lettres) -> 2 erreurs max ("parmnesan" -> "parmesan")
        assertThat(OcrLevenshteinUtils.getMaxAllowedErrors(8)).isEqualTo(2)
        assertThat(OcrLevenshteinUtils.isFuzzyMatch("parmnesan", "parmesan")).isTrue()
    }
}
