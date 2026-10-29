package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.dao.RecipeCategoryInfo
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Recipe
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires pour [IngredientViewModel], [MainViewModel], [RecipeCategoryViewModel],
 * [SearchRecipeViewModel], et [ViewModelFactory].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OtherViewModelsTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application

    private lateinit var mockIngrRepo: IngredientRepository
    private lateinit var mockLocRepo: LocationRepository
    private lateinit var mockRecipeRepo: RecipeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()

        mockIngrRepo = mock()
        mockLocRepo = mock()
        mockRecipeRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `IngredientViewModel should handle search queries and delete`() = runTest {
        val allIngrsLiveData = MutableLiveData<List<Ingredient>>()
        val searchIngrsLiveData = MutableLiveData<List<Ingredient>>()

        whenever(mockIngrRepo.getAll()).thenReturn(allIngrsLiveData)
        whenever(mockIngrRepo.searchInLocation("%lait%", -1L)).thenReturn(searchIngrsLiveData)

        val vm = IngredientViewModel(mockIngrRepo, mockLocRepo)
        val observer = Observer<List<Ingredient>> { }
        vm.ingredients.observeForever(observer)

        vm.setLocation(10L)
        vm.setSearchQuery("")
        allIngrsLiveData.value = listOf(Ingredient(id = 1L, name = "Beurre"))
        assertThat(vm.ingredients.value).hasSize(1)

        vm.setSearchQuery("lait")
        searchIngrsLiveData.value = listOf(Ingredient(id = 2L, name = "Lait"))
        assertThat(vm.ingredients.value).hasSize(1)

        val deleteIngr = Ingredient(id = 3L, name = "Farine")
        vm.delete(deleteIngr).join()
        verify(mockIngrRepo).delete(deleteIngr)

        vm.ingredients.removeObserver(observer)
    }

    @Test
    fun `MainViewModel should delegate ingredient and recipe getters to repository`() {
        val ingrsLiveData = MutableLiveData<List<Ingredient>>(emptyList())
        val recipesLiveData = MutableLiveData<List<Recipe>>(emptyList())

        whenever(mockIngrRepo.getAll()).thenReturn(ingrsLiveData)
        whenever(mockRecipeRepo.allRecipes).thenReturn(recipesLiveData)

        val vm = MainViewModel(mockIngrRepo, mockRecipeRepo)
        assertThat(vm.allIngredients.value).isEmpty()
        assertThat(vm.allRecipes?.value).isEmpty()
    }

    @Test
    fun `RecipeCategoryViewModel should fetch categories with count`() {
        val catLiveData = MutableLiveData<List<RecipeCategoryInfo>>(listOf(RecipeCategoryInfo(1L, "Desserts", 5)))
        whenever(mockRecipeRepo.getRecipeCategoriesWithCount()).thenReturn(catLiveData)

        val vm = RecipeCategoryViewModel(mockRecipeRepo)
        assertThat(vm.recipeCategories.value).hasSize(1)
        assertThat(vm.recipeCategories.value?.get(0)?.categoryName).isEqualTo("Desserts")
    }

    @Test
    fun `toggleIngredientSet helper function should handle null set add and remove`() {
        val setFromNull = SearchRecipeViewModel.toggleIngredientSet(null, "Lait")
        assertThat(setFromNull).contains("Lait")

        val setRemove = SearchRecipeViewModel.toggleIngredientSet(mutableSetOf("Lait", "Oeufs"), "Lait")
        assertThat(setRemove).doesNotContain("Lait")
        assertThat(setRemove).contains("Oeufs")
    }

    @Test
    fun `SearchRecipeViewModel toggleIngredient clearSelection and performSearch should update state`() = runTest {
        val recipesWithIngrsLiveData = MutableLiveData<List<com.example.chiefinventory.dao.RecipeWithIngredients>>(emptyList())
        val allIngrsLiveData = MutableLiveData<List<Ingredient>>(emptyList())

        whenever(mockRecipeRepo.getAllRecipesWithIngredientsLiveData()).thenReturn(recipesWithIngrsLiveData)
        whenever(mockIngrRepo.getAll()).thenReturn(allIngrsLiveData)
        whenever(mockRecipeRepo.searchRecipesByIngredients(listOf("Beurre"))).thenReturn(listOf(Pair(Recipe(id = 1L, title = "Gâteau"), 1)))

        val vm = SearchRecipeViewModel(mockRecipeRepo, mockIngrRepo)

        vm.toggleIngredient("Beurre")
        assertThat(vm.selectedIngredients.value).contains("Beurre")
        assertThat(vm.searchResults.value).hasSize(1)

        vm.toggleIngredient("Beurre") // Toggle off
        assertThat(vm.selectedIngredients.value).doesNotContain("Beurre")
        assertThat(vm.searchResults.value).isEmpty()

        vm.toggleIngredient("Beurre")
        vm.clearSelection()
        assertThat(vm.selectedIngredients.value).isEmpty()
        assertThat(vm.searchResults.value).isEmpty()
    }

    @Test
    fun `ViewModelFactory should create all registered ViewModel classes`() {
        whenever(mockLocRepo.getAll()).thenReturn(MutableLiveData(emptyList()))

        val factory = ViewModelFactory(app, mockLocRepo, mockIngrRepo, mockRecipeRepo, testDispatcher)

        assertThat(factory.create(MainViewModel::class.java)).isNotNull()
        assertThat(factory.create(LocationViewModel::class.java)).isNotNull()
        assertThat(factory.create(InventoryViewModel::class.java)).isNotNull()
        assertThat(factory.create(BackupViewModel::class.java)).isNotNull()
        assertThat(factory.create(IngredientViewModel::class.java)).isNotNull()
        assertThat(factory.create(EditIngredientViewModel::class.java)).isNotNull()
        assertThat(factory.create(MatchRecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(SetupInventoryViewModel::class.java)).isNotNull()
        assertThat(factory.create(ShoppingListViewModel::class.java)).isNotNull()
        assertThat(factory.create(SearchRecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(EditRecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(RecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(RecipeCategoryViewModel::class.java)).isNotNull()
    }
}
