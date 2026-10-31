package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.dao.RecipeDao
import com.chefinventory.app.dao.RecipeWithIngredients
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Recipe
import com.chefinventory.app.model.RecipeIngredient
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MatchRecipeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var app: Application
    private lateinit var mockRecipeDao: RecipeDao
    private lateinit var mockIngrDao: IngredientDao
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        mockRecipeDao = mock()
        mockIngrDao = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCalculateMatches_ExecutesEngineAndUpdatesLiveData() = runTest {
        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(emptyList())
        whenever(mockIngrDao.getAllSync()).thenReturn(emptyList())

        val viewModel = MatchRecipeViewModel(
            application = app,
            ioDispatcher = testDispatcher,
            recipeDao = mockRecipeDao,
            ingredientDao = mockIngrDao
        )

        val job = viewModel.calculateMatches()
        job.join()

        assertThat(viewModel.matchedRecipes.value).isEqualTo(emptyList<Any>())
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCalculateMatches_WithRecipesAndIngredients_EvaluatesMatches() = runTest {
        val recipe = Recipe(id = 1L, title = "Tarte aux pommes")
        val ingredient = RecipeIngredient(id = 10L, recipeId = 1L, ingredientName = "Pommes", quantityRequired = 2.0, unit = "kg")
        val recipeWithIngr = RecipeWithIngredients(recipe = recipe, ingredients = listOf(ingredient))
        val pantryIngr = Ingredient(id = 100L, name = "Pommes", quantity = 3.0, unit = "kg")

        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(listOf(recipeWithIngr))
        whenever(mockIngrDao.getAllSync()).thenReturn(listOf(pantryIngr))

        val viewModel = MatchRecipeViewModel(app, testDispatcher, mockRecipeDao, mockIngrDao)
        viewModel.calculateMatches().join()

        assertThat(viewModel.matchedRecipes.value).isNotNull()
        assertThat(viewModel.matchedRecipes.value).isNotEmpty()
    }
}
