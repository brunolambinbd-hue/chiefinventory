package com.example.parabdcollector.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.util.MainDispatcherRule
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
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: CategoryAuditViewModel

    @Before
    fun setup() {
        repository = mock()
        viewModel = CategoryAuditViewModel(repository)
    }

    @Test
    fun `performAudit should find items with missing or ND super-categories`() = runTest {
        // GIVEN: 3 items. One correct, one with empty super-cat, one with "N/D"
        val mockItems = listOf(
            createItem(1, "Affiches", "Image"),           // Correct -> ignore
            createItem(2, "Affiches", ""),                // Empty -> fix
            createItem(3, "Travaux pour Spirou", "N/D")   // N/D -> fix
        )
        whenever(repository.getAllByTitle("")).thenReturn(mockItems)

        // WHEN: Lancement de l'audit
        viewModel.performAudit()
        advanceUntilIdle() // Attendre la fin de la coroutine

        // THEN: Devrait trouver 2 objets à réparer
        assertEquals(2, viewModel.auditResult.value)
    }

    @Test
    fun `fixInconsistencies should update items with correct super-category from Mapper`() = runTest {
        // GIVEN: Un objet à réparer détecté par l'audit
        val itemToFix = createItem(1, "Affiches", "N/D")
        whenever(repository.getAllByTitle("")).thenReturn(listOf(itemToFix))
        
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
            titre = "Test $id",
            categorie = cat,
            superCategorie = superCat,
            editeur = null, annee = null, mois = null, materiau = null,
            tirage = null, dimensions = null, prixAchat = null, valeurEstimee = null,
            lieuAchat = null, description = null, imageUri = null, imageEmbedding = null,
            locationId = null, remoteId = null, isPossessed = true
        )
    }
}
