package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRecipeRepo: RecipeRepository

    @Before
    fun setUp() {
        mockRecipeRepo = mock()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecipeViewModel_SettersAndFilteringLiveData() {
        val recipe1 = Recipe(id = 1L, title = "Crêpes", instructions = "Mélanger la farine")
        val recipe2 = Recipe(id = 2L, title = "Tarte", instructions = "Cuire la pâte")
        val allList = listOf(recipe1, recipe2)

        whenever(mockRecipeRepo.allRecipes).thenReturn(MutableLiveData(allList))
        whenever(mockRecipeRepo.getRecipesByCategory(10L)).thenReturn(MutableLiveData(listOf(recipe1)))

        val viewModel = RecipeViewModel(mockRecipeRepo)
        viewModel.recipes.observeForever {}

        assertThat(viewModel.recipes.value).containsExactly(recipe1, recipe2)

        viewModel.setSearchQuery("Crêpes")
        assertThat(viewModel.recipes.value).containsExactly(recipe1)

        viewModel.setCategoryId(10L)
        viewModel.setSearchQuery("")
        assertThat(viewModel.recipes.value).containsExactly(recipe1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCompanionHelpers_ResolveBaseRecipesAndFilterRecipes() {
        val recipe1 = Recipe(id = 1L, title = "Mousse Chocolat", instructions = null)
        val recipe2 = Recipe(id = 2L, title = "Gâteau", instructions = "Au chocolat")
        val recipes = listOf(recipe1, recipe2)

        whenever(mockRecipeRepo.allRecipes).thenReturn(MutableLiveData(recipes))
        whenever(mockRecipeRepo.getRecipesByCategory(5L)).thenReturn(MutableLiveData(listOf(recipe1)))

        // resolveBaseRecipes
        assertThat(RecipeViewModel.resolveBaseRecipes(-1L, mockRecipeRepo).value).isEqualTo(recipes)
        assertThat(RecipeViewModel.resolveBaseRecipes(null, mockRecipeRepo).value).isEqualTo(recipes)
        assertThat(RecipeViewModel.resolveBaseRecipes(5L, mockRecipeRepo).value).containsExactly(recipe1)

        // filterRecipes
        assertThat(RecipeViewModel.filterRecipes(recipes, null)).hasSize(2)
        assertThat(RecipeViewModel.filterRecipes(recipes, "   ")).hasSize(2)
        assertThat(RecipeViewModel.filterRecipes(recipes, "Chocolat")).containsExactly(recipe1, recipe2)
        assertThat(RecipeViewModel.filterRecipes(recipes, "Mousse")).containsExactly(recipe1)
        assertThat(RecipeViewModel.filterRecipes(recipes, "Inexistant")).isEmpty()
    }
}
