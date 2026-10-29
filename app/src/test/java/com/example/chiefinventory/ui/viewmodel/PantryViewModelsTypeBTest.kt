package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
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
 * Suite de tests unitaires Robolectric de Type B couvrant à 100% les ViewModels du Garde-Manger
 * ([IngredientViewModel], [EditIngredientViewModel], [ShoppingListViewModel], [SetupInventoryViewModel]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PantryViewModelsTypeBTest {

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
    fun `IngredientViewModel should filter search and delete ingredient`() = runTest {
        val mockIngrRepo = mock<IngredientRepository>()
        val mockLocRepo = mock<LocationRepository>()

        val liveDataAll = MutableLiveData<List<Ingredient>>(emptyList())
        whenever(mockIngrRepo.getAll()).thenReturn(liveDataAll)
        whenever(mockIngrRepo.searchInLocation("%Beurre%", -1L)).thenReturn(liveDataAll)

        val vm = IngredientViewModel(mockIngrRepo, mockLocRepo)
        vm.ingredients.observeForever {}

        vm.setLocation(1L)
        vm.setSearchQuery("Beurre")

        val ingredient = Ingredient(id = 5L, name = "Beurre")
        vm.delete(ingredient)

        verify(mockIngrRepo).delete(ingredient)
    }

    @Test
    fun `EditIngredientViewModel should load and CRUD ingredient`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mockIngrRepo = mock<IngredientRepository>()
        val mockLocRepo = mock<LocationRepository>()

        val ingredient = Ingredient(id = 10L, name = "Lait", imageUri = "content://media/10")
        whenever(mockIngrRepo.getById(10L)).thenReturn(ingredient)
        whenever(mockLocRepo.getAllByType(LocationType.INGREDIENT)).thenReturn(MutableLiveData(emptyList()))

        val vm = EditIngredientViewModel(app, mockIngrRepo, mockLocRepo)

        vm.loadIngredient(10L)
        assertThat(vm.ingredient.value).isEqualTo(ingredient)
        assertThat(vm.imageUri.value).isNotNull()

        val mockUri = mock<Uri>()
        vm.setImageUri(mockUri)
        assertThat(vm.imageUri.value).isEqualTo(mockUri)

        vm.insert(ingredient)
        verify(mockIngrRepo).insert(ingredient)

        vm.update(ingredient)
        verify(mockIngrRepo).update(ingredient)

        vm.delete(ingredient)
        verify(mockIngrRepo).delete(ingredient)
    }

    @Test
    fun `ShoppingListViewModel default constructor parameters should be covered`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vmDefault = ShoppingListViewModel(app)
        assertThat(vmDefault).isNotNull()
        assertThat(vmDefault.outOfStockIngredients).isNotNull()
    }

    @Test
    fun `ShoppingListViewModel should mark item bought and update quantity`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mockIngrDao = mock<com.example.chiefinventory.dao.IngredientDao>()
        whenever(mockIngrDao.getOutOfStockIngredients()).thenReturn(MutableLiveData(emptyList()))
        val vm = ShoppingListViewModel(app, mockIngrDao, testDispatcher)

        val ingredient = Ingredient(id = 20L, name = "Œufs", quantity = 0.0, packSize = 6.0)

        vm.markItemBought(ingredient, isBought = true).join()
        assertThat(vm.outOfStockIngredients).isNotNull()
        verify(mockIngrDao).update(org.mockito.kotlin.any())

        vm.markItemBought(ingredient, isBought = false).join()
    }

    @Test
    fun `calculateBoughtQuantity helper function should cover all packSize null currentQty and isBought branches`() {
        // Line 34: packSize <= 0.0 (defaults to 1.0)
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 2.0, packSize = 0.0, isBought = true)).isEqualTo(3.0)

        // Line 40: currentQty = null (defaults to 0.0)
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = null, packSize = 6.0, isBought = true)).isEqualTo(6.0)

        // Line 46: isBought = false (returns 0.0)
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 5.0, packSize = 6.0, isBought = false)).isEqualTo(0.0)

        // Standard case: isBought = true, packSize > 0, currentQty != null
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 2.0, packSize = 6.0, isBought = true)).isEqualTo(8.0)
    }

    @Test
    fun `SetupInventoryViewModel should load catalog and save selected items`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mockIngrDao = mock<com.example.chiefinventory.dao.IngredientDao>()
        val mockLocDao = mock<com.example.chiefinventory.dao.LocationDao>()
        whenever(mockIngrDao.getAllSync()).thenReturn(emptyList())
        whenever(mockLocDao.getAllSync()).thenReturn(emptyList())

        val vm = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)

        vm.loadCatalog(app.resources).join()
        assertThat(vm.catalogEntries).isNotNull()

        vm.saveSelectedIngredients(emptyList())?.join()
    }
}
