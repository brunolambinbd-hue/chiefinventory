package com.example.chiefinventory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class CategoryAuditViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: CategoryAuditViewModel

    @Before
    fun setup() {
        repository = mock()
        // Injection du testDispatcher de la règle pour remplacer Dispatchers.IO
        viewModel = CategoryAuditViewModel(repository, mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `performAudit should find items with missing or ND super-categories`(): Unit = runTest {
        // GIVEN: 3 items. One correct, one with empty super-cat, one with "N/D"
        val mockItems = listOf(
            createItem(1, "Affiches", "Image"),           // Correct -> ignore
            createItem(2, "Affiches", ""),                // Empty -> fix
            createItem(3, "Travaux pour Spirou", "N/D")   // N/D -> fix
        )
        whenever(repository.getAllItemsSuspend()).thenReturn(mockItems)

        // WHEN: Lancement de l'audit
        viewModel.performAudit()
        advanceUntilIdle() 

        // THEN: Devrait trouver 2 objets à réparer
        assertEquals(2, viewModel.auditResult.value)
    }

    @Test
    fun `fixInconsistencies should update items with correct super-category from Mapper`(): Unit = runTest {
        // GIVEN : Un objet à réparer détecté par l'audit
        val itemToFix = createItem(1, "Affiches", "N/D")
        whenever(repository.getAllItemsSuspend()).thenReturn(listOf(itemToFix))
        
        viewModel.performAudit()
        advanceUntilIdle()

        // WHEN: Réparation
        viewModel.fixInconsistencies()
        advanceUntilIdle()

        // THEN: Le repository doit recevoir un update avec "Image" (règle pour Affiches)
        verify(repository).update(argThat { 
            this.id == 1L && this.superCategorie == "Image" 
        })
        assertEquals(1, viewModel.updateStatus.value)
        assertNull(viewModel.auditResult.value)
    }

    private fun createItem(id: Long, cat: String, superCat: String?): CollectionItem {
        return CollectionItem(
            id = id,
            titre = "Objet de collection $id",
            categorie = cat,
            superCategorie = superCat,
            editeur = "Éditions du Test",
            annee = 2024,
            mois = 5,
            materiau = "Plomb",
            tirage = "1000 ex.",
            dimensions = "12 x 15 cm",
            prixAchat = 45.0,
            valeurEstimee = 65.0,
            lieuAchat = "Boutique spécialisée",
            description = "Une description détaillée pour l'objet de test $id."
        )
    }
}
