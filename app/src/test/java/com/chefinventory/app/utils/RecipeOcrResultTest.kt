package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class RecipeOcrResultTest {

    @Test
    fun recipeOcrResult_canBeInstantiatedWithDefaultNulls() {
        val result = RecipeOcrResult()
        assertWithMessage("Le titre de RecipeOcrResult par défaut doit être null")
            .that(result.title)
            .isNull()

        assertWithMessage("Les ingrédients de RecipeOcrResult par défaut doivent être null")
            .that(result.ingredients)
            .isNull()
    }

    @Test
    fun recipeOcrResult_assignsPropertiesCorrectly() {
        val result = RecipeOcrResult(title = "Cake", kcalPerServing = "300")
        assertWithMessage("Le titre de RecipeOcrResult doit valoir 'Cake'")
            .that(result.title)
            .isEqualTo("Cake")

        assertWithMessage("Les kilocalories de RecipeOcrResult doivent valoir '300'")
            .that(result.kcalPerServing)
            .isEqualTo("300")
    }
}
