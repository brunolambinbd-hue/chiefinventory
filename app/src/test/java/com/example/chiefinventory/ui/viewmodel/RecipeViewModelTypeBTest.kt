package com.example.chiefinventory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires pour [RecipeViewModel] et [RecipeViewModel.Companion.filterRecipes].
 */
class RecipeViewModelTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepo: RecipeRepository

    @Before
    fun setUp() {
        mockRepo = mock()
    }

    @Test
    fun `filterRecipes companion helper should filter title and instructions`() {
        val r1 = Recipe(id = 1L, title = "Crêpes Sucre", instructions = "Mélanger la farine")
        val r2 = Recipe(id = 2L, title = "Gaufres Liège", instructions = "Cuire au gaufrier")
        val r3 = Recipe(id = 3L, title = "Omelette", instructions = "Battre les œufs")
        val recipes = listOf(r1, r2, r3)

        // Query null or blank -> returns all
        assertThat(RecipeViewModel.filterRecipes(recipes, null)).hasSize(3)
        assertThat(RecipeViewModel.filterRecipes(recipes, "   ")).hasSize(3)

        // Title match
        val titleMatch = RecipeViewModel.filterRecipes(recipes, "crêpes")
        assertThat(titleMatch).containsExactly(r1)

        // Instructions match
        val instructionMatch = RecipeViewModel.filterRecipes(recipes, "gaufrier")
        assertThat(instructionMatch).containsExactly(r2)
    }

    @Test
    fun `resolveBaseRecipes helper function should return correct LiveData for null -1 and specific category`() {
        val allLiveData = MutableLiveData<List<Recipe>>()
        val categoryLiveData = MutableLiveData<List<Recipe>>()

        whenever(mockRepo.allRecipes).thenReturn(allLiveData)
        whenever(mockRepo.getRecipesByCategory(10L)).thenReturn(categoryLiveData)

        assertThat(RecipeViewModel.resolveBaseRecipes(null, mockRepo)).isEqualTo(allLiveData)
        assertThat(RecipeViewModel.resolveBaseRecipes(-1L, mockRepo)).isEqualTo(allLiveData)
        assertThat(RecipeViewModel.resolveBaseRecipes(10L, mockRepo)).isEqualTo(categoryLiveData)
    }

    @Test
    fun `RecipeViewModel should handle searchQuery update when baseRecipes is null`() {
        val nullRecipesLiveData = MutableLiveData<List<Recipe>>()
        whenever(mockRepo.allRecipes).thenReturn(nullRecipesLiveData)

        val vm = RecipeViewModel(mockRepo)
        val observer = Observer<List<Recipe>> { }
        vm.recipes.observeForever(observer)

        // Base recipes value is null -> line 31 emptyList() executed
        vm.setSearchQuery("Crêpe")
        assertThat(vm.recipes.value).isEmpty()

        vm.recipes.removeObserver(observer)
    }

    @Test
    fun `RecipeViewModel should switch categoryId between default -1 and specific category`() {
        val allRecipesLiveData = MutableLiveData<List<Recipe>>()
        val categoryRecipesLiveData = MutableLiveData<List<Recipe>>()

        whenever(mockRepo.allRecipes).thenReturn(allRecipesLiveData)
        whenever(mockRepo.getRecipesByCategory(10L)).thenReturn(categoryRecipesLiveData)

        val vm = RecipeViewModel(mockRepo)
        val observer = Observer<List<Recipe>> { }
        vm.recipes.observeForever(observer)

        // Default category -1L
        val r1 = Recipe(id = 1L, title = "Tarte")
        allRecipesLiveData.value = listOf(r1)
        assertThat(vm.recipes.value).containsExactly(r1)

        vm.setSearchQuery("Tarte")
        assertThat(vm.recipes.value).containsExactly(r1)

        // Switch to category 10L
        vm.setSearchQuery("")
        val r2 = Recipe(id = 2L, title = "Gâteau")
        vm.setCategoryId(10L)
        categoryRecipesLiveData.value = listOf(r2)
        assertThat(vm.recipes.value).containsExactly(r2)

        // Switch back to category -1L
        vm.setCategoryId(-1L)
        assertThat(vm.recipes.value).containsExactly(r1)

        vm.recipes.removeObserver(observer)
    }
}
