package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.dao.LocationDao
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.model.StockPolicy
import com.chefinventory.app.model.StandardCatalogItem
import com.chefinventory.app.ui.adapter.SelectedCatalogEntry
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetupInventoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var app: Application
    private lateinit var mockIngrDao: IngredientDao
    private lateinit var mockLocDao: LocationDao
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        mockIngrDao = mock()
        mockLocDao = mock()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDefaultConstructor_InstantiatesWithDefaultParameters() {
        val defaultVm = SetupInventoryViewModel(app)
        assertThat(defaultVm.catalogEntries.value).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadCatalog_WithEmptyAndExistingIngredientsAndForceSelectAll() = runTest(testDispatcher) {
        val existingIngr = Ingredient(id = 1L, name = "beurre", quantity = null, locationId = 10L)
        whenever(mockIngrDao.getAllSync()).thenReturn(listOf(existingIngr))

        val viewModel = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        val res = app.resources

        // Force select all = false on existing BDD
        viewModel.loadCatalog(res, forceSelectAll = false).join()
        assertThat(viewModel.catalogEntries.value).isNotNull()

        // Force select all = true
        viewModel.loadCatalog(res, forceSelectAll = true).join()
        assertThat(viewModel.catalogEntries.value).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveSelectedIngredients_EmptyList_ReturnsNull() {
        val viewModel = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        val job = viewModel.saveSelectedIngredients(emptyList())
        assertThat(job).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveSelectedIngredients_WithEmptyLocationsAndDuplicates() = runTest(testDispatcher) {
        // Location Dao initially returns empty, then populates locations
        val locFrigo = Location(id = 1L, name = "Frigo", parentId = null, type = LocationType.INGREDIENT)
        whenever(mockLocDao.getAllSync()).thenReturn(emptyList(), listOf(locFrigo))

        val duplicate1 = Ingredient(id = 10L, name = "Beurre", quantity = 1.0, locationId = 1L)
        val duplicate2 = Ingredient(id = 20L, name = "Beurre", quantity = 2.0, locationId = 1L)
        whenever(mockIngrDao.getAllSync()).thenReturn(listOf(duplicate1, duplicate2))

        val catItem = StandardCatalogItem(name = "Beurre", defaultUnit = "g", category = "Crémerie", defaultLocationName = "Frigo", stockPolicy = StockPolicy.PERMANENT, defaultQuantity = 250.0)
        val selectedEntry = SelectedCatalogEntry(catItem, isSelected = true, currentQuantity = 250.0)

        val viewModel = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        val job = viewModel.saveSelectedIngredients(listOf(selectedEntry))
        job?.join()

        // Verify default locations created
        verify(mockLocDao).insert(Location(name = "Frigo", parentId = null, type = LocationType.INGREDIENT))
        verify(mockIngrDao).update(any())
        verify(mockIngrDao).delete(duplicate2)
        assertThat(viewModel.saveStatus.value).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveSelectedIngredients_UpdateWithNullLocationAndInsertWithNullLocation() = runTest(testDispatcher) {
        // Locations list does NOT contain any location matching "Inconnue"
        val locCave = Location(id = 1L, name = "Cave", parentId = null, type = LocationType.INGREDIENT)
        whenever(mockLocDao.getAllSync()).thenReturn(listOf(locCave))

        // Existing ingredient with primary location ID = 99L
        val existingIngr = Ingredient(id = 50L, name = "Lait", quantity = 1.0, locationId = 99L)
        whenever(mockIngrDao.getAllSync()).thenReturn(listOf(existingIngr))

        // 1. Item matching existing "Lait", but with defaultLocationName = "Inconnue" (loc will be null -> falls back to primary.locationId 99L)
        val item1 = StandardCatalogItem(name = "Lait", defaultUnit = "L", category = "Crémerie", defaultLocationName = "Inconnue", stockPolicy = StockPolicy.PERMANENT, defaultQuantity = 1.0)
        val entry1 = SelectedCatalogEntry(item1, isSelected = true, currentQuantity = 1.0)

        // 2. New Item "Écrevisses", defaultLocationName = "Inconnue" (loc will be null -> targetLocationId = null)
        val item2 = StandardCatalogItem(name = "Écrevisses", defaultUnit = "g", category = "Poissonnerie", defaultLocationName = "Inconnue", stockPolicy = StockPolicy.ON_DEMAND, defaultQuantity = 200.0)
        val entry2 = SelectedCatalogEntry(item2, isSelected = true, currentQuantity = 200.0)

        val viewModel = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        viewModel.saveSelectedIngredients(listOf(entry1, entry2))?.join()

        verify(mockIngrDao).update(any())
        verify(mockIngrDao).insert(any())
        assertThat(viewModel.saveStatus.value).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSaveSelectedIngredients_NewIngredientWithTargetQuantity() = runTest(testDispatcher) {
        val locPlacard = Location(id = 2L, name = "Placard", parentId = null, type = LocationType.INGREDIENT)
        whenever(mockLocDao.getAllSync()).thenReturn(listOf(locPlacard))
        whenever(mockIngrDao.getAllSync()).thenReturn(emptyList())

        val catItem = StandardCatalogItem(name = "Farine", defaultUnit = "kg", category = "Épicerie", defaultLocationName = "Placard", stockPolicy = StockPolicy.PERMANENT, defaultQuantity = 1.0)
        val selectedEntry = SelectedCatalogEntry(catItem, isSelected = true, currentQuantity = 1.0)

        val viewModel = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        viewModel.saveSelectedIngredients(listOf(selectedEntry))?.join()

        verify(mockIngrDao).insert(any())
        assertThat(viewModel.saveStatus.value).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCompanionHelpers_DetermineEntrySelectionAndQuantityAndTarget() {
        // determineEntrySelection
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = true, stockPolicy = StockPolicy.PERMANENT, hasMatch = false, matchQuantity = null)).isTrue()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = true, stockPolicy = StockPolicy.ON_DEMAND, hasMatch = false, matchQuantity = null)).isFalse()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = StockPolicy.PERMANENT, hasMatch = true, matchQuantity = 5.0)).isTrue()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = StockPolicy.PERMANENT, hasMatch = true, matchQuantity = null)).isFalse()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = StockPolicy.PERMANENT, hasMatch = true, matchQuantity = 0.0)).isFalse()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = StockPolicy.PERMANENT, hasMatch = false, matchQuantity = null)).isFalse()

        // determineEntryQuantity
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = 10.0, defaultQuantity = 1.0)).isEqualTo(10.0)
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = 0.0, defaultQuantity = 2.0)).isEqualTo(2.0)
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = null, defaultQuantity = 2.0)).isEqualTo(2.0)

        // calculateTargetQuantity
        assertThat(SetupInventoryViewModel.calculateTargetQuantity(isSelected = true, currentQuantity = 5.0)).isEqualTo(5.0)
        assertThat(SetupInventoryViewModel.calculateTargetQuantity(isSelected = false, currentQuantity = 5.0)).isEqualTo(0.0)
    }
}
