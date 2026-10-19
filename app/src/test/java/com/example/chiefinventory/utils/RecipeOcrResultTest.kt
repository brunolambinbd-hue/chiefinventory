package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecipeOcrResultTest {

    @Test
    fun recipeOcrResult_canBeInstantiatedWithDefaultNulls() {
        val result = RecipeOcrResult()
        assertThat(result.title).isNull()
        assertThat(result.ingredients).isNull()
    }

    @Test
    fun recipeOcrResult_assignsPropertiesCorrectly() {
        val result = RecipeOcrResult(title = "Cake", kcalPerServing = "300")
        assertThat(result.title).isEqualTo("Cake")
        assertThat(result.kcalPerServing).isEqualTo("300")
    }
}
