package com.example.chiefinventory.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.viewmodel.EditItemViewModel
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EditItemViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var collectionRepository: CollectionRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var application: Application
    private lateinit var viewModel: EditItemViewModel

    @Before
    fun setup() {
        collectionRepository = mock()
        locationRepository = mock()
        application = mock()

        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))

        viewModel = EditItemViewModel(application, collectionRepository, locationRepository)
    }

    @Test
    fun `loadItem should fetch item from repository and update LiveData`() {
        val itemId = 123L
        val mockItem = createTestItem(id = itemId, titre = "Gaston Lagaffe - Tome 1")
        val liveData = MutableLiveData<CollectionItem>()
        whenever(collectionRepository.getById(itemId)).thenReturn(liveData)

        val observer = Observer<CollectionItem> { }
        viewModel.item.observeForever(observer)

        viewModel.loadItem(itemId)
        liveData.value = mockItem

        assertThat(viewModel.item.value).isEqualTo(mockItem)

        viewModel.item.removeObserver(observer)
    }

    @Test
    fun `insert should call insert on repository`(): Unit = runTest {
        val newItem = createTestItem(titre = "Spirou et Fantasio - Virus")

        viewModel.insert(newItem)

        verify(collectionRepository).insert(newItem)
    }

    @Test
    fun `update should call update on repository`(): Unit = runTest {
        val updatedItem = createTestItem(id = 1, titre = "Tintin au Tibet - Édition Spéciale")

        viewModel.update(updatedItem)

        verify(collectionRepository).update(updatedItem)
    }

    private fun createTestItem(id: Long = 0, titre: String): CollectionItem {
        return CollectionItem(
            id = id,
            titre = titre,
            editeur = "Dupuis",
            annee = 2023,
            mois = 10,
            categorie = "Albums",
            superCategorie = "Bandes Dessinées",
            materiau = "Papier",
            tirage = "5000 ex.",
            dimensions = "22x30 cm",
            prixAchat = 15.50,
            valeurEstimee = 25.0,
            lieuAchat = "Librairie spécialisée",
            description = "Un superbe album de test avec tous ses détails renseignés.",
            isPossessed = false
        )
    }
}
