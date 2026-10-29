package com.example.chiefinventory.repo

import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SearchCriteria
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B assurant la couverture des méthodes de [CollectionRepository].
 */
class CollectionRepositoryMethodsTypeBTest {

    @Test
    fun `basic query methods should delegate to collectionDao`() = runTest {
        val mockDao = mock<CollectionDao>()
        val liveData = MutableLiveData<List<CollectionItem>>(emptyList())
        val liveDataCount = MutableLiveData<Int>(10)

        whenever(mockDao.getAll()).thenReturn(liveData)
        whenever(mockDao.getUnlocatedItems()).thenReturn(liveData)
        whenever(mockDao.getLocatedNotPossessedItems()).thenReturn(liveData)
        whenever(mockDao.getItemsByLocationId(1L)).thenReturn(liveData)
        whenever(mockDao.getRecentPossessed()).thenReturn(liveData)
        whenever(mockDao.getRecentLocated()).thenReturn(liveData)
        whenever(mockDao.getAllPossessed()).thenReturn(liveData)
        whenever(mockDao.getAllSought()).thenReturn(liveData)
        whenever(mockDao.getTotalCount()).thenReturn(liveDataCount)

        val repository = CollectionRepository(mockDao)

        assertThat(repository.getAll()).isEqualTo(liveData)
        assertThat(repository.getUnlocatedItems()).isEqualTo(liveData)
        assertThat(repository.getLocatedNotPossessedItems()).isEqualTo(liveData)
        assertThat(repository.getItemsByLocationId(1L)).isEqualTo(liveData)
        assertThat(repository.getRecentPossessed()).isEqualTo(liveData)
        assertThat(repository.getRecentLocated()).isEqualTo(liveData)
        assertThat(repository.getAllPossessed()).isEqualTo(liveData)
        assertThat(repository.getAllSought()).isEqualTo(liveData)
        assertThat(repository.getTotalCount()).isEqualTo(liveDataCount)
    }

    @Test
    fun `suspend search methods should delegate to collectionDao`() = runTest {
        val mockDao = mock<CollectionDao>()
        val item = CollectionItem(id = 1L, titre = "Spirou")
        val itemsList = listOf(item)

        whenever(mockDao.getAllSuspend()).thenReturn(itemsList)
        whenever(mockDao.getItemById(1L)).thenReturn(item)
        whenever(mockDao.findByRemoteId(101)).thenReturn(item)
        whenever(mockDao.search("%Spirou%")).thenReturn(itemsList)
        whenever(mockDao.getAllByTitle("%Spirou%")).thenReturn(itemsList)

        val repository = CollectionRepository(mockDao)

        assertThat(repository.getAllItemsSuspend()).isEqualTo(itemsList)
        assertThat(repository.getItemById(1L)).isEqualTo(item)
        assertThat(repository.findByRemoteId(101)).isEqualTo(item)
        assertThat(repository.search("Spirou")).isEqualTo(itemsList)
        assertThat(repository.getAllByTitle("Spirou")).isEqualTo(itemsList)
    }

    @Test
    fun `advancedSearch with text criteria should query collectionDao`() = runTest {
        val mockDao = mock<CollectionDao>()
        val item = CollectionItem(id = 1L, titre = "Spirou", editeur = "Dupuis", isPossessed = true)

        whenever(mockDao.advancedSearch(any())).thenReturn(listOf(item))
        whenever(mockDao.countAdvancedSearch(any())).thenReturn(1)

        val repository = CollectionRepository(mockDao)
        val criteria = SearchCriteria(
            titre = "Spirou",
            editeur = "Dupuis",
            annee = 1980,
            mois = 5,
            superCategorie = "Bande Dessinée",
            categorie = "Spirou",
            description = "Aventure",
            tirage = "Standard",
            dimensions = "A4",
            isPossessed = true
        )

        val result = repository.advancedSearch(criteria, qE = null, qOcr = "Spirou")

        assertThat(result.totalMatchesCount).isEqualTo(1)
        assertThat(result.displayedItems).hasSize(1)
        assertThat(result.displayedItems.first().item.titre).isEqualTo("Spirou")
    }
}
