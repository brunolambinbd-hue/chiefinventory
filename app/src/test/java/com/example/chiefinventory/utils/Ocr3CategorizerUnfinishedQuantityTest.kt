package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la détection et la fusion des lignes d'unités/quantités incomplètes
 * par [Ocr3CategorizerUnfinishedIngredient].
 */
class Ocr3CategorizerUnfinishedQuantityTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie le recollage de lignes d'unités isolées ("1 cuillère à soupe" + "de moutarde") en un ingrédient unique.
     */
    @Test
    fun `categorize should merge unfinished quantity unit lines like 1 cuillere a soupe with next line de moutarde`() {
        val input = listOf(
            "ingrédients",
            "1 cuillère à soupe",
            "de moutarde",
            "2 c. à soupe",
            "de crème fraîche"
        )

        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.rawIngredientsList).contains("1 cuillère à soupe de moutarde")
        assertThat(result.rawIngredientsList).contains("2 c. à soupe de crème fraîche")
        assertThat(result.rawIngredientsList).doesNotContain("1 cuillère à soupe")
        assertThat(result.rawIngredientsList).doesNotContain("2 c. à soupe")
    }
}
