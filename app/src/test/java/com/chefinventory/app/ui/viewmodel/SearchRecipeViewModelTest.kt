package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SearchRecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRecipeRepo: RecipeRepository
    private lateinit var mockIngrRepo: IngredientRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRecipeRepo = mock()
        mockIngrRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSearchRecipeViewModel_ToggleClearAndSearch() = runTest {
        val recipe = Recipe(id = 1L, title = "Omelette")
        whenever(mockRecipeRepo.getAllRecipesWithIngredientsLiveData()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockIngrRepo.getAll()).thenReturn(MutableLiveData(listOf(Ingredient(name = "Oeufs"))))
        whenever(mockRecipeRepo.searchRecipesByIngredients(any())).thenReturn(listOf(Pair(recipe, 1)))

        val viewModel = SearchRecipeViewModel(mockRecipeRepo, mockIngrRepo)

        // Read LiveData properties
        assertThat(viewModel.recipesWithIngredients.value).isEmpty()
        assertThat(viewModel.allIngredients.value).hasSize(1)

        viewModel.toggleIngredient("Oeufs")
        assertThat(viewModel.selectedIngredients.value).contains("Oeufs")
        assertThat(viewModel.searchResults.value).contains(Pair(recipe, 1))

        // Untoggle
        viewModel.toggleIngredient("Oeufs")
        assertThat(viewModel.selectedIngredients.value).isEmpty()
        assertThat(viewModel.searchResults.value).isEmpty()

        // Clear selection
        viewModel.toggleIngredient("Lait")
        viewModel.clearSelection()
        assertThat(viewModel.selectedIngredients.value).isEmpty()
        assertThat(viewModel.searchResults.value).isEmpty()

        // Test performSearch with null selectedIngredients value
        val selectedIngrLiveData = viewModel.selectedIngredients as MutableLiveData<MutableSet<String>>
        selectedIngrLiveData.value = null
        viewModel.performSearch()
        assertThat(viewModel.searchResults.value).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCompanion_ToggleIngredientSet_HandlesNullAndExisting() {
        val setFromNull = SearchRecipeViewModel.toggleIngredientSet(null, "Farine")
        assertThat(setFromNull).contains("Farine")

        val setRemove = SearchRecipeViewModel.toggleIngredientSet(mutableSetOf("Farine"), "Farine")
        assertThat(setRemove).doesNotContain("Farine")
    }
}
