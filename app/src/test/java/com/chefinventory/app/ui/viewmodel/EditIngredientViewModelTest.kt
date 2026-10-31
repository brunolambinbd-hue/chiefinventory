package com.chefinventory.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric pour [EditIngredientViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditIngredientViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var app: Application
    private lateinit var mockIngrRepo: IngredientRepository
    private lateinit var mockLocRepo: LocationRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        mockIngrRepo = mock()
        mockLocRepo = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadIngredient_WithImageUri_LoadsCorrectly() = runTest {
        whenever(mockLocRepo.getAllByType(any())).thenReturn(MutableLiveData(emptyList()))
        val ingredient = Ingredient(id = 10L, name = "Beurre", imageUri = "file:///tmp/beurre.jpg")
        whenever(mockIngrRepo.getById(10L)).thenReturn(ingredient)

        val vm = EditIngredientViewModel(app, mockIngrRepo, mockLocRepo)
        vm.loadIngredient(10L)

        assertThat(vm.ingredient.value).isEqualTo(ingredient)
        assertThat(vm.imageUri.value).isEqualTo(Uri.parse("file:///tmp/beurre.jpg"))
        assertThat(vm.ingredientCategories.value).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadIngredient_NullImageUriOrNotFound() = runTest {
        val ingredientNoImage = Ingredient(id = 20L, name = "Lait", imageUri = null)
        whenever(mockIngrRepo.getById(20L)).thenReturn(ingredientNoImage)
        whenever(mockIngrRepo.getById(99L)).thenReturn(null)

        val vm = EditIngredientViewModel(app, mockIngrRepo, mockLocRepo)

        vm.loadIngredient(20L)
        assertThat(vm.ingredient.value).isEqualTo(ingredientNoImage)
        assertThat(vm.imageUri.value).isNull()

        vm.loadIngredient(99L)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSetImageUri_UpdatesLiveData() {
        val vm = EditIngredientViewModel(app, mockIngrRepo, mockLocRepo)
        val uri = Uri.parse("file:///tmp/test.jpg")

        vm.setImageUri(uri)
        assertThat(vm.imageUri.value).isEqualTo(uri)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testInsertUpdateDelete_DelegatesToRepository() = runTest {
        val vm = EditIngredientViewModel(app, mockIngrRepo, mockLocRepo)
        val ingr = Ingredient(id = 1L, name = "Farine")

        vm.insert(ingr).join()
        verify(mockIngrRepo).insert(ingr)

        vm.update(ingr).join()
        verify(mockIngrRepo).update(ingr)

        vm.delete(ingr).join()
        verify(mockIngrRepo).delete(ingr)
    }
}
