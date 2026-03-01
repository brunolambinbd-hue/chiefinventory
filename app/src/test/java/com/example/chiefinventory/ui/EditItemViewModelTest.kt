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
 * Unit tests for the [com.example.chiefinventory.ui.viewmodel.EditItemViewModel].
 */
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

        // Mock la dépendance de la liste des emplacements qui est lue dans le `init`
        whenever(locationRepository.getAll()).thenReturn(MutableLiveData(emptyList()))

        viewModel = EditItemViewModel(application, collectionRepository, locationRepository)
    }

    @Test
    fun `loadItem should fetch item from repository and update LiveData`() {
        // GIVEN: Le repository est programmé pour retourner un LiveData pour un item spécifique.
        val itemId = 123L
        val mockItem = createTestItem(id = itemId, titre = "Gaston Lagaffe - Tome 1")
        val liveData = MutableLiveData<CollectionItem>()
        whenever(collectionRepository.getById(itemId)).thenReturn(liveData)

        val observer = Observer<CollectionItem> { }
        viewModel.item.observeForever(observer)

        // WHEN: On charge l'item
        viewModel.loadItem(itemId)
        liveData.value = mockItem

        // THEN: Le LiveData doit être mis à jour avec l'objet complet.
        assertEquals(mockItem, viewModel.item.value)

        viewModel.item.removeObserver(observer)
    }

    @Test
    fun `insert should call insert on repository`(): Unit = runTest {
        // GIVEN : Un nouvel item réaliste à insérer (id = 0 par défaut).
        val newItem = createTestItem(titre = "Spirou et Fantasio - Virus")

        // WHEN : La fonction insert est appelée.
        viewModel.insert(newItem)

        // THEN: La méthode insert du repository doit être appelée avec l'objet complet.
        verify(collectionRepository).insert(newItem)
    }

    @Test
    fun `update should call update on repository`(): Unit = runTest {
        // GIVEN : Un item existant mis à jour.
        val updatedItem = createTestItem(id = 1, titre = "Tintin au Tibet - Édition Spéciale")

        // WHEN: La fonction update est appelée.
        viewModel.update(updatedItem)

        // THEN: La méthode update du repository doit être appelée avec l'objet complet.
        verify(collectionRepository).update(updatedItem)
    }

    /**
     * Crée un objet CollectionItem avec des données réalistes pour les tests.
     */
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
