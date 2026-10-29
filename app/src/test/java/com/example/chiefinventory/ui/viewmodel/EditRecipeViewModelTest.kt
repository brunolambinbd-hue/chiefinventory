package com.example.chiefinventory.ui.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires pour [EditRecipeViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditRecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockRecipeRepo: RecipeRepository
    private lateinit var mockLocRepo: LocationRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRecipeRepo = mock()
        mockLocRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRecipe and getIngredientsForRecipe should query recipeRepository`() = runTest {
        val recipe = Recipe(id = 10L, title = "Crêpes")
        whenever(mockRecipeRepo.getRecipeById(10L)).thenReturn(recipe)

        val ingredientsLiveData = MutableLiveData<List<RecipeIngredient>>(emptyList())
        whenever(mockRecipeRepo.getIngredientsForRecipe(10L)).thenReturn(ingredientsLiveData)

        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocRepo)
        vm.loadRecipe(10L)

        assertThat(vm.recipe.value).isEqualTo(recipe)
        assertThat(vm.getIngredientsForRecipe(10L)).isEqualTo(ingredientsLiveData)
    }

    @Test
    fun `setImageUri should update LiveData`() {
        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocRepo)
        val uri = Uri.parse("file:///tmp/recipe.jpg")

        vm.setImageUri(uri)
        assertThat(vm.imageUri.value).isEqualTo(uri)
    }

    @Test
    fun `insert should set recipeId on ingredients and save to repository`() = runTest {
        whenever(mockRecipeRepo.insertRecipe(anyRecipe())).thenReturn(50L)

        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocRepo)
        val recipe = Recipe(title = "Gaufres")
        val ingr1 = RecipeIngredient(recipeId = 0L, ingredientName = "Farine", quantityRequired = 250.0)

        vm.insert(recipe, listOf(ingr1))

        verify(mockRecipeRepo).insertRecipe(recipe)
        verify(mockRecipeRepo).insertRecipeIngredients(listOf(ingr1.copy(recipeId = 50L)))
    }

    @Test
    fun `update should update recipe and ingredients`() = runTest {
        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocRepo)
        val recipe = Recipe(id = 30L, title = "Tarte")
        val ingr1 = RecipeIngredient(recipeId = 30L, ingredientName = "Pommes", quantityRequired = 3.0)

        vm.update(recipe, listOf(ingr1))
        verify(mockRecipeRepo).updateRecipe(recipe)
        verify(mockRecipeRepo).deleteIngredientsByRecipeId(30L)
        verify(mockRecipeRepo).insertRecipeIngredients(listOf(ingr1))
    }

    @Test
    fun `delete should delete recipe and ingredients`() = runTest {
        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocRepo)
        val recipe = Recipe(id = 30L, title = "Tarte")

        vm.delete(recipe)
        verify(mockRecipeRepo).deleteIngredientsByRecipeId(30L)
        verify(mockRecipeRepo).deleteRecipe(recipe)
    }

    private fun anyRecipe(): Recipe = org.mockito.kotlin.any()
}
