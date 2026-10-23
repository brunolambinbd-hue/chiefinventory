package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrIngredientUtils].
 */
class OcrIngredientUtilsTest {

    @Test
    fun `hasExplicitWeight should detect weight and volume units`() {
        assertThat(OcrIngredientUtils.hasExplicitWeight("200 g de farine")).isTrue()
        assertThat(OcrIngredientUtils.hasExplicitWeight("5 cl d'huile")).isTrue()
        assertThat(OcrIngredientUtils.hasExplicitWeight("Sel et poivre")).isFalse()
    }

    @Test
    fun `countIngredientSequences should count distinct ingredient sequences`() {
        assertThat(OcrIngredientUtils.countIngredientSequences("2 Carottes 1 Pomme")).isEqualTo(2)
        assertThat(OcrIngredientUtils.countIngredientSequences("2 Carottes")).isEqualTo(1)
    }

    @Test
    fun `cleanIngredientSemantics should strip bullets and empty non-letter lines`() {
        assertThat(OcrIngredientUtils.cleanIngredientSemantics("• Tomate", emptyList())).isEqualTo("Tomate")
        assertThat(OcrIngredientUtils.cleanIngredientSemantics("!!!", emptyList())).isEmpty()
    }
}
