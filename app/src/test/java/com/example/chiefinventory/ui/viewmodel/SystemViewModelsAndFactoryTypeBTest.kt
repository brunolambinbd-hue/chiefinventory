package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.repo.CollectionRepository
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
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B assurant la couverture de [ViewModelFactory]
 * et des ViewModels Système & Maintenance ([MainViewModel], [BackupViewModel]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SystemViewModelsAndFactoryTypeBTest {

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
    fun `ViewModelFactory should create supported ViewModel instances`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mockCollectionRepo = mock<CollectionRepository>()
        val mockLocationRepo = mock<LocationRepository>()
        val mockIngredientRepo = mock<IngredientRepository>()
        val mockRecipeRepo = mock<RecipeRepository>()

        whenever(mockCollectionRepo.getAll()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockCollectionRepo.getSignatureStats()).thenReturn(MutableLiveData())
        whenever(mockCollectionRepo.getTotalCount()).thenReturn(MutableLiveData(0))
        whenever(mockCollectionRepo.getAllPossessed()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockCollectionRepo.getAllSought()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockCollectionRepo.getRecentPossessed()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockCollectionRepo.getRecentLocated()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockIngredientRepo.getAll()).thenReturn(MutableLiveData(emptyList()))
        whenever(mockLocationRepo.getAllByType(any())).thenReturn(MutableLiveData(emptyList()))
        whenever(mockLocationRepo.getAll()).thenReturn(MutableLiveData(emptyList()))

        val factory = ViewModelFactory(app, mockCollectionRepo, mockLocationRepo, mockIngredientRepo, mockRecipeRepo, testDispatcher)

        assertThat(factory.create(MainViewModel::class.java)).isNotNull()
        assertThat(factory.create(SignatureReportViewModel::class.java)).isNotNull()
        assertThat(factory.create(LocationViewModel::class.java)).isNotNull()
        assertThat(factory.create(BackupViewModel::class.java)).isNotNull()
        assertThat(factory.create(BatchPossessionViewModel::class.java)).isNotNull()
        assertThat(factory.create(CategoryAuditViewModel::class.java)).isNotNull()
        assertThat(factory.create(IngredientViewModel::class.java)).isNotNull()
        assertThat(factory.create(SearchRecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(EditRecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(RecipeViewModel::class.java)).isNotNull()
        assertThat(factory.create(RecipeCategoryViewModel::class.java)).isNotNull()
    }

    @Test
    fun `ViewModelFactory should throw IllegalArgumentException when required recipeRepository is null`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val mockCollectionRepo = mock<CollectionRepository>()
        val mockLocationRepo = mock<LocationRepository>()
        val mockIngredientRepo = mock<IngredientRepository>()

        val factory = ViewModelFactory(app, mockCollectionRepo, mockLocationRepo, mockIngredientRepo, recipeRepository = null, testDispatcher)

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(RecipeViewModel::class.java)
        }
    }

    @Test
    fun `MainViewModel should observe total items and all ingredients`() {
        val mockCollectionRepo = mock<CollectionRepository>()
        val mockIngredientRepo = mock<IngredientRepository>()

        whenever(mockCollectionRepo.getTotalCount()).thenReturn(MutableLiveData(100))
        whenever(mockIngredientRepo.getAll()).thenReturn(MutableLiveData(emptyList()))

        val vm = MainViewModel(mockCollectionRepo, mockIngredientRepo)

        assertThat(vm.totalItemsCount).isNotNull()
        assertThat(vm.allIngredients).isNotNull()
    }

    @Test
    fun `BackupViewModel should expose operationStatus LiveData`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = BackupViewModel(app)

        assertThat(vm.operationStatus).isNotNull()
    }
}
