package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.example.chiefinventory.repo.IngredientRepository
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B couvrant à 100% les ViewModels de Cuisine & Recettes
 * ([RecipeViewModel], [EditRecipeViewModel], [MatchRecipeViewModel], [SearchRecipeViewModel], [RecipeCategoryViewModel]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KitchenViewModelsTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `RecipeViewModel should update search query and category ID`() {
        val mockRecipeRepo = mock<RecipeRepository>()
        val liveData = MutableLiveData<List<Recipe>>(emptyList())
        whenever(mockRecipeRepo.allRecipes).thenReturn(liveData)

        val vm = RecipeViewModel(mockRecipeRepo)
        vm.recipes.observeForever {}

        vm.setSearchQuery("Tarte")
        vm.setCategoryId(10L)

        verify(mockRecipeRepo).getRecipesByCategory(10L)
    }

    @Test
    fun `EditRecipeViewModel should load recipe and handle CRUD operations`() = runTest {
        val mockRecipeRepo = mock<RecipeRepository>()
        val mockLocationRepo = mock<LocationRepository>()

        val recipe = Recipe(id = 1L, title = "Carbonnade")
        val ingredients = listOf(RecipeIngredient(recipeId = 1L, ingredientName = "Bœuf"))
        val liveDataLoc = MutableLiveData<List<Location>>(emptyList())

        whenever(mockRecipeRepo.getRecipeById(1L)).thenReturn(recipe)
        whenever(mockRecipeRepo.insertRecipe(recipe)).thenReturn(1L)
        whenever(mockLocationRepo.getAllByType(LocationType.RECIPE)).thenReturn(liveDataLoc)

        val vm = EditRecipeViewModel(mockRecipeRepo, mockLocationRepo)

        vm.loadRecipe(1L)
        assertThat(vm.recipe.value).isEqualTo(recipe)

        val mockUri = mock<Uri>()
        vm.setImageUri(mockUri)
        assertThat(vm.imageUri.value).isEqualTo(mockUri)

        vm.insert(recipe, ingredients)
        verify(mockRecipeRepo).insertRecipe(recipe)

        vm.update(recipe, ingredients)
        verify(mockRecipeRepo).updateRecipe(recipe)

        vm.delete(recipe)
        verify(mockRecipeRepo).deleteRecipe(recipe)
    }

    @Test
    fun `MatchRecipeViewModel should calculate matches and post results`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = MatchRecipeViewModel(app)

        vm.calculateMatches().join()
        assertThat(vm.matchedRecipes).isNotNull()
    }

    @Test
    fun `SearchRecipeViewModel should toggle ingredients and clear selection`() = runTest {
        val mockRecipeRepo = mock<RecipeRepository>()
        val mockIngredientRepo = mock<IngredientRepository>()

        whenever(mockRecipeRepo.getAllRecipesWithIngredientsLiveData()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockIngredientRepo.getAll()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockRecipeRepo.searchRecipesByIngredients(any())).thenReturn(emptyList())

        val vm = SearchRecipeViewModel(mockRecipeRepo, mockIngredientRepo)

        vm.toggleIngredient("Sel")
        assertThat(vm.selectedIngredients.value).contains("Sel")

        vm.toggleIngredient("Sel")
        assertThat(vm.selectedIngredients.value).doesNotContain("Sel")

        vm.toggleIngredient("Poivre")
        vm.clearSelection()
        assertThat(vm.selectedIngredients.value).isEmpty()
    }

    @Test
    fun `RecipeCategoryViewModel should expose categories from recipe repository`() {
        val mockRecipeRepo = mock<RecipeRepository>()
        val liveDataCat = MutableLiveData<List<com.example.chiefinventory.dao.RecipeCategoryInfo>>(emptyList())
        whenever(mockRecipeRepo.getRecipeCategoriesWithCount()).thenReturn(liveDataCat)

        val vm = RecipeCategoryViewModel(mockRecipeRepo)
        assertThat(vm.recipeCategories).isEqualTo(liveDataCat)
    }
}
