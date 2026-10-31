package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.model.Ingredient
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
class ShoppingListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var app: Application
    private lateinit var mockIngrDao: IngredientDao
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        mockIngrDao = mock()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testDefaultConstructors_InstantiatesWithDefaultParameters() {
        val defaultVm1 = ShoppingListViewModel(app)
        assertThat(defaultVm1).isNotNull()

        val defaultVm2 = ShoppingListViewModel(app, mockIngrDao)
        assertThat(defaultVm2).isNotNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testMarkItemBought_UpdatesQuantityInDao() = runTest(testDispatcher) {
        val ingr = Ingredient(id = 1L, name = "Beurre", quantity = 0.0, packSize = 1.0)
        whenever(mockIngrDao.getOutOfStockIngredients()).thenReturn(MutableLiveData(listOf(ingr)))

        val viewModel = ShoppingListViewModel(app, mockIngrDao, testDispatcher)
        assertThat(viewModel.outOfStockIngredients.value).containsExactly(ingr)

        viewModel.markItemBought(ingr, isBought = true).join()
        verify(mockIngrDao).update(any())
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCalculateBoughtQuantity_HandlesBoughtUnboughtNullQuantityAndPackSizes() {
        // Is bought = true
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 0.0, packSize = 6.0, isBought = true)).isEqualTo(6.0)
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = null, packSize = 1.0, isBought = true)).isEqualTo(1.0)
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 2.0, packSize = -1.0, isBought = true)).isEqualTo(3.0)

        // Is bought = false
        assertThat(ShoppingListViewModel.calculateBoughtQuantity(currentQty = 5.0, packSize = 1.0, isBought = false)).isEqualTo(0.0)
    }
}
