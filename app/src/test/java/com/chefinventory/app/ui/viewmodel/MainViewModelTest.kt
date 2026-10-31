package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMainViewModel_WithNonNullAndNullRecipeRepository() {
        val mockIngrRepo = mock<IngredientRepository>()
        val mockRecipeRepo = mock<RecipeRepository>()

        val ingrList = listOf(Ingredient(id = 1L, name = "Lait"))
        val recipeList = listOf(Recipe(id = 10L, title = "Crêpes"))

        whenever(mockIngrRepo.getAll()).thenReturn(MutableLiveData(ingrList))
        whenever(mockIngrRepo.getOutOfStockIngredients()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockRecipeRepo.allRecipes).thenReturn(MutableLiveData(recipeList))

        // Non-null recipeRepository
        val vmWithRecipes = MainViewModel(mockIngrRepo, mockRecipeRepo)
        assertThat(vmWithRecipes.allIngredients.value).isEqualTo(ingrList)
        assertThat(vmWithRecipes.outOfStockIngredients.value).isEmpty()
        assertThat(vmWithRecipes.allRecipes?.value).isEqualTo(recipeList)

        // Null recipeRepository passed explicitly
        val vmNullRecipes = MainViewModel(mockIngrRepo, null)
        assertThat(vmNullRecipes.allRecipes).isNull()

        // Default constructor parameter (recipeRepository omitted)
        val vmDefault = MainViewModel(mockIngrRepo)
        assertThat(vmDefault.allRecipes).isNull()
    }
}
