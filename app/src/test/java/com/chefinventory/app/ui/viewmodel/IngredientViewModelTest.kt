package com.chefinventory.app.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.LocationRepository
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IngredientViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockIngrRepo: IngredientRepository
    private lateinit var mockLocRepo: LocationRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockIngrRepo = mock()
        mockLocRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIngredientViewModel_SettersDeleteAndLiveData() = runTest {
        val ingrList = listOf(Ingredient(id = 1L, name = "Beurre"))
        whenever(mockIngrRepo.getAll()).thenReturn(MutableLiveData(ingrList))

        val viewModel = IngredientViewModel(mockIngrRepo, mockLocRepo)
        viewModel.setLocation(5L)
        viewModel.setSearchQuery("Beurre")

        assertThat(viewModel.ingredients.value).isEqualTo(ingrList)

        val ingrToDelete = Ingredient(id = 10L, name = "Lait")
        viewModel.delete(ingrToDelete).join()
        verify(mockIngrRepo).delete(ingrToDelete)
    }
}
