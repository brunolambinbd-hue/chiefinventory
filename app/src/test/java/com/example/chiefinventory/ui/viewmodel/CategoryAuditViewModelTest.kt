package com.example.chiefinventory.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
        viewModel = CategoryAuditViewModel(repository, mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `performAudit should find items with missing or ND super-categories`(): Unit = runTest {
        val mockItems = listOf(
            createItem(1, "Affiches", "Image"),
            createItem(2, "Affiches", ""),
            createItem(3, "Travaux pour Spirou", "N/D")
        )
        whenever(repository.getAllItemsSuspend()).thenReturn(mockItems)

        viewModel.performAudit()
        advanceUntilIdle()

        assertThat(viewModel.auditResult.value).isEqualTo(2)
    }

    @Test
    fun `fixInconsistencies should update items with correct super-category from Mapper`(): Unit = runTest {
        val itemToFix = createItem(1, "Affiches", "N/D")
        whenever(repository.getAllItemsSuspend()).thenReturn(listOf(itemToFix))
        
        viewModel.performAudit()
        advanceUntilIdle()

        viewModel.fixInconsistencies()
        advanceUntilIdle()

        verify(repository).update(argThat { 
            this.id == 1L && this.superCategorie == "Image" 
        })
        assertThat(viewModel.updateStatus.value).isEqualTo(1)
        assertThat(viewModel.auditResult.value).isNull()
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
