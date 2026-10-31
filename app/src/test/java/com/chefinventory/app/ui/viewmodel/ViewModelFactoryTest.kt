package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.LocationRepository
import com.chefinventory.app.repo.RecipeRepository
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class UnknownTestViewModel : ViewModel()

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ViewModelFactoryTest {

    private lateinit var app: Application
    private lateinit var mockLocRepo: LocationRepository
    private lateinit var mockIngrRepo: IngredientRepository
    private lateinit var mockRecipeRepo: RecipeRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        mockLocRepo = mock()
        mockIngrRepo = mock()
        mockRecipeRepo = mock()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testViewModelFactory_CreatesAllSupportedViewModels() {
        val factory = ViewModelFactory(
            application = app,
            locationRepository = mockLocRepo,
            ingredientRepository = mockIngrRepo,
            recipeRepository = mockRecipeRepo,
            ioDispatcher = testDispatcher
        )

        assertThat(factory.create(MainViewModel::class.java)).isInstanceOf(MainViewModel::class.java)
        assertThat(factory.create(LocationViewModel::class.java)).isInstanceOf(LocationViewModel::class.java)
        assertThat(factory.create(InventoryViewModel::class.java)).isInstanceOf(InventoryViewModel::class.java)
        assertThat(factory.create(BackupViewModel::class.java)).isInstanceOf(BackupViewModel::class.java)
        assertThat(factory.create(IngredientViewModel::class.java)).isInstanceOf(IngredientViewModel::class.java)
        assertThat(factory.create(EditIngredientViewModel::class.java)).isInstanceOf(EditIngredientViewModel::class.java)
        assertThat(factory.create(MatchRecipeViewModel::class.java)).isInstanceOf(MatchRecipeViewModel::class.java)
        assertThat(factory.create(SetupInventoryViewModel::class.java)).isInstanceOf(SetupInventoryViewModel::class.java)
        assertThat(factory.create(ShoppingListViewModel::class.java)).isInstanceOf(ShoppingListViewModel::class.java)
        assertThat(factory.create(SearchRecipeViewModel::class.java)).isInstanceOf(SearchRecipeViewModel::class.java)
        assertThat(factory.create(EditRecipeViewModel::class.java)).isInstanceOf(EditRecipeViewModel::class.java)
        assertThat(factory.create(RecipeViewModel::class.java)).isInstanceOf(RecipeViewModel::class.java)
        assertThat(factory.create(RecipeCategoryViewModel::class.java)).isInstanceOf(RecipeCategoryViewModel::class.java)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testViewModelFactory_DefaultConstructorParameters_InstantiatesSuccessfully() {
        val factoryWithDefaults = ViewModelFactory(
            application = app,
            locationRepository = mockLocRepo,
            ingredientRepository = mockIngrRepo
        )
        assertThat(factoryWithDefaults.create(MainViewModel::class.java)).isInstanceOf(MainViewModel::class.java)
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testViewModelFactory_NullRecipeRepository_ThrowsIllegalArgumentException() {
        val factoryNullRecipe = ViewModelFactory(
            application = app,
            locationRepository = mockLocRepo,
            ingredientRepository = mockIngrRepo,
            recipeRepository = null,
            ioDispatcher = testDispatcher
        )

        assertThrows(IllegalArgumentException::class.java) {
            factoryNullRecipe.create(SearchRecipeViewModel::class.java)
        }
        assertThrows(IllegalArgumentException::class.java) {
            factoryNullRecipe.create(EditRecipeViewModel::class.java)
        }
        assertThrows(IllegalArgumentException::class.java) {
            factoryNullRecipe.create(RecipeViewModel::class.java)
        }
        assertThrows(IllegalArgumentException::class.java) {
            factoryNullRecipe.create(RecipeCategoryViewModel::class.java)
        }
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testViewModelFactory_UnknownViewModelClass_ThrowsIllegalArgumentException() {
        val factory = ViewModelFactory(
            application = app,
            locationRepository = mockLocRepo,
            ingredientRepository = mockIngrRepo,
            recipeRepository = mockRecipeRepo,
            ioDispatcher = testDispatcher
        )

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnknownTestViewModel::class.java)
        }
    }
}
