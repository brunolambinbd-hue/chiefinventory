package com.example.chiefinventory.utils

import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.dao.RecipeWithIngredients
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B vérifiant la mise à jour automatique de [Recipe.imageUri]
 * lors de la ré-importation d'un Pack par pays.
 */
class StarterRecipeSeedLoaderImageUriUpdateTypeBTest {

    @Test
    fun `importSeedListToDatabase should update imageUri for existing recipe in SQLite Room`() = runTest {
        val mockDb = mock<AppDatabase>()
        val mockRecipeDao = mock<RecipeDao>()
        val mockLocationDao = mock<LocationDao>()

        val existingRecipe = Recipe(id = 88L, title = "Chicons au gratin", imageUri = null)
        val existingWithIngr = RecipeWithIngredients(recipe = existingRecipe, ingredients = emptyList())

        whenever(mockDb.recipeDao()).thenReturn(mockRecipeDao)
        whenever(mockDb.locationDao()).thenReturn(mockLocationDao)
        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(listOf(existingWithIngr))
        whenever(mockLocationDao.getAllSync()).thenReturn(emptyList())

        val seeds = listOf(
            StarterRecipeSeedLoader.SeedRecipe(
                title = "Chicons au gratin",
                category = "Plat principal",
                cuisineOrigin = "Cuisine Belge",
                prepTimeMinutes = 15,
                cookTimeMinutes = 35,
                servings = 4,
                imageUrl = "file:///android_asset/recipe_images/chicons_au_gratin.jpg",
                ingredients = listOf("4 chicons", "4 tranches de jambon"),
                instructions = listOf("Rouler les chicons dans le jambon"),
                sourceUrl = "https://fr.wikibooks.org/wiki/Chicons_au_gratin"
            )
        )

        val result = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, mockDb)

        val captor = argumentCaptor<Recipe>()
        verify(mockRecipeDao).updateRecipe(captor.capture())

        val updated = captor.firstValue
        assertThat(updated.imageUri).isEqualTo("file:///android_asset/recipe_images/chicons_au_gratin.jpg")
        assertThat(result.updatedCount).isEqualTo(1)
    }
}
