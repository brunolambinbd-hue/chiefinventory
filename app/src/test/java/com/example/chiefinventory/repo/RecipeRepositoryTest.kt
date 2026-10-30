package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.dao.RecipeWithIngredients
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests unitaires dédiés (1-à-1) pour le repository [RecipeRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRecipeDao: RecipeDao
    private lateinit var repository: RecipeRepository
    private val allRecipesLiveData = MutableLiveData<List<Recipe>>(emptyList())

    @Before
    fun setUp() {
        mockRecipeDao = mock()
        whenever(mockRecipeDao.getAllRecipes()).thenReturn(allRecipesLiveData)
        repository = RecipeRepository(mockRecipeDao)
    }

    @Test
    fun `normalize and isIngredientMatching helper functions should be covered atomically`() {
        // Normalization
        assertThat(RecipeRepository.normalize("Épinards à la crème")).isEqualTo("epinardsalacreme")

        // Matching branches
        // Branch 1: recipeIng.contains(availIng) -> true
        assertThat(RecipeRepository.isIngredientMatching("chiconsdebruxelles", "chicons")).isTrue()

        // Branch 2: availIng.contains(recipeIng) -> true
        assertThat(RecipeRepository.isIngredientMatching("chicons", "chiconsdebruxelles")).isTrue()

        // Branch 3: No match -> false
        assertThat(RecipeRepository.isIngredientMatching("carottes", "chicons")).isFalse()
    }

    @Test
    fun `calculateRecipeMatchCount should return correct count`() {
        val recipeIngs = listOf("chicons", "jambon", "creme")
        val available = listOf("chicons", "jambon")

        val matchCount = RecipeRepository.calculateRecipeMatchCount(recipeIngs, available)
        assertThat(matchCount).isEqualTo(2)
    }

    @Test
    fun `repository delegation getters and CRUD operations should execute successfully`() = runTest {
        assertThat(repository.allRecipes).isEqualTo(allRecipesLiveData)

        val withIngrsLiveData = MutableLiveData<List<RecipeWithIngredients>>(emptyList())
        whenever(mockRecipeDao.getAllRecipesWithIngredientsLiveData()).thenReturn(withIngrsLiveData)
        assertThat(repository.getAllRecipesWithIngredientsLiveData()).isEqualTo(withIngrsLiveData)

        val byCategoryLiveData = MutableLiveData<List<Recipe>>(emptyList())
        whenever(mockRecipeDao.getRecipesByCategory(org.mockito.kotlin.any())).thenReturn(byCategoryLiveData)
        assertThat(repository.getRecipesByCategory(10L)).isEqualTo(byCategoryLiveData)

        val recipe = Recipe(id = 5L, title = "Tarte")
        whenever(mockRecipeDao.getRecipeById(org.mockito.kotlin.any())).thenReturn(recipe)
        assertThat(repository.getRecipeById(5L)).isEqualTo(recipe)

        whenever(mockRecipeDao.insertRecipe(recipe)).thenReturn(5L)
        assertThat(repository.insertRecipe(recipe)).isEqualTo(5L)

        repository.updateRecipe(recipe)
        verify(mockRecipeDao).updateRecipe(recipe)

        repository.deleteRecipe(recipe)
        verify(mockRecipeDao).deleteRecipe(recipe)

        val ingrLiveData = MutableLiveData<List<RecipeIngredient>>(emptyList())
        whenever(mockRecipeDao.getIngredientsForRecipe(5L)).thenReturn(ingrLiveData)
        assertThat(repository.getIngredientsForRecipe(5L)).isEqualTo(ingrLiveData)

        repository.deleteIngredientsByRecipeId(5L)
        verify(mockRecipeDao).deleteIngredientsByRecipeId(5L)

        val catCountLiveData = MutableLiveData<List<RecipeCategoryInfo>>(emptyList())
        whenever(mockRecipeDao.getRecipeCategoriesWithCount()).thenReturn(catCountLiveData)
        assertThat(repository.getRecipeCategoriesWithCount()).isEqualTo(catCountLiveData)
    }

    @Test
    fun `searchRecipesByIngredients should return matched recipes sorted by score`() = runTest {
        val recipe1 = Recipe(id = 1L, title = "Chicons au gratin")
        val recipeIngredients1 = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Chicons"), RecipeIngredient(recipeId = 1L, ingredientName = "Jambon"))
        val item1 = RecipeWithIngredients(recipe = recipe1, ingredients = recipeIngredients1)

        val recipe2 = Recipe(id = 2L, title = "Salade verte")
        val recipeIngredients2 = listOf(RecipeIngredient(recipeId = 2L, ingredientName = "Laitue"))
        val item2 = RecipeWithIngredients(recipe = recipe2, ingredients = recipeIngredients2)

        val recipe3 = Recipe(id = 3L, title = "Eau de cuisson")
        val item3 = RecipeWithIngredients(recipe = recipe3, ingredients = emptyList())

        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(listOf(item1, item2, item3))

        val results = repository.searchRecipesByIngredients(listOf("chicons", "jambon"))
        assertThat(results).hasSize(1)
        assertThat(results[0].first).isEqualTo(recipe1)
        assertThat(results[0].second).isEqualTo(2)
    }

    @Test
    fun `insertRecipeIngredients should return on empty and delegate on non empty`() = runTest {
        repository.insertRecipeIngredients(emptyList())

        val ingrList = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Jambon"))
        repository.insertRecipeIngredients(ingrList)

        verify(mockRecipeDao).insertRecipeIngredients(ingrList)
    }
}
