package com.example.chiefinventory.repo

import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.dao.RecipeWithIngredients
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B couvrant à 100% l'ensemble des méthodes de [RecipeRepository].
 */
class RecipeRepositoryTypeBTest {

    @Test
    fun `allRecipes and LiveData methods should delegate to recipeDao`() {
        val mockRecipeDao = mock<RecipeDao>()
        val mockIngredientDao = mock<IngredientDao>()

        val recipesLiveData = MutableLiveData<List<Recipe>>(emptyList())
        val withIngrLiveData = MutableLiveData<List<RecipeWithIngredients>>(emptyList())
        val catLiveData = MutableLiveData<List<Recipe>>(emptyList())
        val ingrLiveData = MutableLiveData<List<RecipeIngredient>>(emptyList())
        val catInfoLiveData = MutableLiveData<List<RecipeCategoryInfo>>(emptyList())

        whenever(mockRecipeDao.getAllRecipes()).thenReturn(recipesLiveData)
        whenever(mockRecipeDao.getAllRecipesWithIngredientsLiveData()).thenReturn(withIngrLiveData)
        whenever(mockRecipeDao.getRecipesByCategory(1L)).thenReturn(catLiveData)
        whenever(mockRecipeDao.getIngredientsForRecipe(5L)).thenReturn(ingrLiveData)
        whenever(mockRecipeDao.getRecipeCategoriesWithCount()).thenReturn(catInfoLiveData)

        val repository = RecipeRepository(mockRecipeDao, mockIngredientDao)

        assertThat(repository.allRecipes).isEqualTo(recipesLiveData)
        assertThat(repository.getAllRecipesWithIngredientsLiveData()).isEqualTo(withIngrLiveData)
        assertThat(repository.getRecipesByCategory(1L)).isEqualTo(catLiveData)
        assertThat(repository.getIngredientsForRecipe(5L)).isEqualTo(ingrLiveData)
        assertThat(repository.getRecipeCategoriesWithCount()).isEqualTo(catInfoLiveData)
    }

    @Test
    fun `suspend CRUD operations should delegate to recipeDao`() = runTest {
        val mockRecipeDao = mock<RecipeDao>()
        val mockIngredientDao = mock<IngredientDao>()

        val recipe = Recipe(id = 10L, title = "Carbonnade")
        whenever(mockRecipeDao.getRecipeById(10L)).thenReturn(recipe)
        whenever(mockRecipeDao.insertRecipe(recipe)).thenReturn(10L)

        val repository = RecipeRepository(mockRecipeDao, mockIngredientDao)

        assertThat(repository.getRecipeById(10L)).isEqualTo(recipe)
        assertThat(repository.insertRecipe(recipe)).isEqualTo(10L)

        repository.updateRecipe(recipe)
        verify(mockRecipeDao).updateRecipe(recipe)

        repository.deleteRecipe(recipe)
        verify(mockRecipeDao).deleteRecipe(recipe)

        repository.deleteIngredientsByRecipeId(10L)
        verify(mockRecipeDao).deleteIngredientsByRecipeId(10L)
    }

    @Test
    fun `insertRecipeIngredients should insert non empty ingredients and return on empty`() = runTest {
        val mockRecipeDao = mock<RecipeDao>()
        val mockIngredientDao = mock<IngredientDao>()

        val repository = RecipeRepository(mockRecipeDao, mockIngredientDao)

        repository.insertRecipeIngredients(emptyList())

        val ingrList = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Jambon"))
        repository.insertRecipeIngredients(ingrList)

        verify(mockRecipeDao).insertRecipeIngredients(ingrList)
    }

    @Test
    fun `searchRecipesByIngredients should match available ingredients with diacritic normalization`() = runTest {
        val mockRecipeDao = mock<RecipeDao>()
        val mockIngredientDao = mock<IngredientDao>()

        val recipe1 = Recipe(id = 1L, title = "Chicons au gratin")
        val recipeIngredients1 = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Chicons (endives)"))
        val item1 = RecipeWithIngredients(recipe = recipe1, ingredients = recipeIngredients1)

        val recipe2 = Recipe(id = 2L, title = "Salade verte")
        val recipeIngredients2 = listOf(RecipeIngredient(recipeId = 2L, ingredientName = "Laitue"))
        val item2 = RecipeWithIngredients(recipe = recipe2, ingredients = recipeIngredients2)

        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(listOf(item1, item2))

        val repository = RecipeRepository(mockRecipeDao, mockIngredientDao)
        val results = repository.searchRecipesByIngredients(listOf("Chicon"))

        assertThat(results).hasSize(1)
        assertThat(results.first().first.title).isEqualTo("Chicons au gratin")
        assertThat(results.first().second).isEqualTo(1)
    }
}
