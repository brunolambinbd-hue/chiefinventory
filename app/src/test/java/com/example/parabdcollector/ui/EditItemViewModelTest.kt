package com.example.parabdcollector.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [EditItemViewModel].
 */
@ExperimentalCoroutinesApi
class EditItemViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var collectionRepository: CollectionRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var application: Application
    private lateinit var viewModel: EditItemViewModel

    @Before
    fun setup() {
        collectionRepository = mock()
        locationRepository = mock()
        application = mock()

        // Mock la dépendance de la liste des emplacements qui est lue dans le `init`
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))

        viewModel = EditItemViewModel(application, collectionRepository, locationRepository)
    }

    @Test
    fun `loadItem should fetch item from repository and update LiveData`() {
        // GIVEN: Le repository est programmé pour retourner un LiveData pour un item spécifique.
        val itemId = 123L
        val mockItem = CollectionItem(id = itemId, remoteId = null, titre = "Test Item", editeur = "", annee = 2023, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, isPossessed = true)
        val liveData = MutableLiveData<CollectionItem>() // Créé vide
        whenever(collectionRepository.getById(itemId)).thenReturn(liveData)

        // On attache un observateur pour activer le MediatorLiveData
        val observer = Observer<CollectionItem> { }
        viewModel.item.observeForever(observer)

        // WHEN: On charge l'item, ce qui attache la source au Mediator.
        viewModel.loadItem(itemId)
        // ET QUAND: La donnée est émise.
        liveData.value = mockItem

        // THEN: Le LiveData de l'item dans le ViewModel doit être mis à jour.
        assertEquals(mockItem, viewModel.item.value)

        // CLEANUP
        viewModel.item.removeObserver(observer)
    }

    @Test
    fun `insert should call insert on repository`() = runTest {
        // GIVEN: Un nouvel item à insérer.
        val newItem = CollectionItem(id = 0, remoteId = null, titre = "New Item", editeur = "", annee = 2023, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, isPossessed = true)

        // WHEN: La fonction insert est appelée.
        viewModel.insert(newItem)

        // THEN: La méthode insert du repository doit être appelée avec le même item.
        verify(collectionRepository).insert(newItem)
    }

    @Test
    fun `update should call update on repository`() = runTest {
        // GIVEN: Un item existant à mettre à jour.
        val updatedItem = CollectionItem(id = 1, remoteId = null, titre = "Updated Item", editeur = "", annee = 2023, mois = null, categorie = null, superCategorie = null, materiau = null, tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null, lieuAchat = null, description = "", imageUri = null, imageEmbedding = null, locationId = null, isPossessed = true)

        // WHEN: La fonction update est appelée.
        viewModel.update(updatedItem)

        // THEN: La méthode update du repository doit être appelée avec le même item.
        verify(collectionRepository).update(updatedItem)
    }
}
