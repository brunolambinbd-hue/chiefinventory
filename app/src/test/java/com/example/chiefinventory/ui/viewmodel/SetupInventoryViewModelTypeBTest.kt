package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.StandardCatalogItem
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.google.common.truth.Truth.assertThat
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

/**
 * Suite de tests unitaires approfondie pour [SetupInventoryViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetupInventoryViewModelTypeBTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application
    private lateinit var mockIngrDao: IngredientDao
    private lateinit var mockLocDao: LocationDao
    private lateinit var mockRes: Resources

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        mockIngrDao = mock()
        mockLocDao = mock()
        mockRes = app.resources
    }

    @Test
    fun `determineEntrySelection should evaluate fresh setup and existing match states`() {
        // Fresh setup -> PERMANENT is selected, ON_DEMAND is not
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = true, stockPolicy = "PERMANENT", hasMatch = false, matchQuantity = null)).isTrue()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = true, stockPolicy = "ON_DEMAND", hasMatch = false, matchQuantity = null)).isFalse()

        // Existing setup -> selected if hasMatch and matchQuantity > 0
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = "PERMANENT", hasMatch = true, matchQuantity = 2.0)).isTrue()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = "PERMANENT", hasMatch = true, matchQuantity = 0.0)).isFalse()
        assertThat(SetupInventoryViewModel.determineEntrySelection(isFreshSetup = false, stockPolicy = "PERMANENT", hasMatch = false, matchQuantity = null)).isFalse()
    }

    @Test
    fun `determineEntryQuantity and calculateTargetQuantity should handle null and zero quantities`() {
        // determineEntryQuantity
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = 5.0, defaultQuantity = 1.0)).isEqualTo(5.0)
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = 0.0, defaultQuantity = 1.0)).isEqualTo(1.0)
        assertThat(SetupInventoryViewModel.determineEntryQuantity(matchQuantity = null, defaultQuantity = 2.0)).isEqualTo(2.0)

        // calculateTargetQuantity
        assertThat(SetupInventoryViewModel.calculateTargetQuantity(isSelected = true, currentQuantity = 3.0)).isEqualTo(3.0)
        assertThat(SetupInventoryViewModel.calculateTargetQuantity(isSelected = false, currentQuantity = 3.0)).isEqualTo(0.0)
        assertThat(SetupInventoryViewModel.calculateTargetQuantity(isSelected = true, currentQuantity = 0.0)).isEqualTo(0.0)
    }

    @Test
    fun `loadCatalog should load entries and post to LiveData`() = runTest {
        whenever(mockIngrDao.getAllSync()).thenReturn(emptyList())

        val vm = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        vm.loadCatalog(mockRes, forceSelectAll = false).join()

        val entries = vm.catalogEntries.value ?: emptyList()
        assertThat(entries).isNotEmpty()
    }

    @Test
    fun `saveSelectedIngredients with empty list should return null`() {
        val vm = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)
        val job = vm.saveSelectedIngredients(emptyList())
        assertThat(job).isNull()
    }

    @Test
    fun `saveSelectedIngredients should update primary ingredient delete duplicates or insert new item`() = runTest {
        val locFrigo = Location(id = 100L, name = "Frigo", parentId = null)
        whenever(mockLocDao.getAllSync()).thenReturn(listOf(locFrigo))

        val primaryBeurre = Ingredient(id = 1L, name = "Beurre", quantity = 0.0, locationId = null)
        val dupBeurre = Ingredient(id = 2L, name = "beurre ", quantity = 0.0, locationId = null)
        whenever(mockIngrDao.getAllSync()).thenReturn(listOf(primaryBeurre, dupBeurre))

        val vm = SetupInventoryViewModel(app, mockIngrDao, mockLocDao, testDispatcher)

        val catItemBeurre = StandardCatalogItem(name = "Beurre", category = "Crémerie", defaultUnit = "g", defaultQuantity = 250.0, defaultLocationName = "Frigo", stockPolicy = "PERMANENT")
        val catItemLait = StandardCatalogItem(name = "Lait", category = "Crémerie", defaultUnit = "L", defaultQuantity = 1.0, defaultLocationName = "Frigo", stockPolicy = "PERMANENT")

        val entryUpdateBeurre = SelectedCatalogEntry(catItemBeurre, isSelected = true, currentQuantity = 250.0)
        val entryInsertLait = SelectedCatalogEntry(catItemLait, isSelected = true, currentQuantity = 1.0)
        val entryUnselected = SelectedCatalogEntry(StandardCatalogItem(name = "Farine", category = "Épicerie", defaultUnit = "kg", defaultQuantity = 1.0, defaultLocationName = "Placard", stockPolicy = "PERMANENT"), isSelected = false, currentQuantity = 1.0)

        vm.saveSelectedIngredients(listOf(entryUpdateBeurre, entryInsertLait, entryUnselected))?.join()

        // Primary update & duplicate deletion verified
        verify(mockIngrDao).update(any())
        verify(mockIngrDao).delete(dupBeurre)
        verify(mockIngrDao).insert(any())
        assertThat(vm.saveStatus.value).isTrue()
    }
}
