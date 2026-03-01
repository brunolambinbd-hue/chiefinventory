package com.example.chiefinventory.repo

import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.model.CollectionItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class CollectionRepositoryTimestampTest {

    private lateinit var mockDao: CollectionDao
    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        mockDao = mock()
        repository = CollectionRepository(mockDao)
    }

    @Test
    fun `insert should refresh updatedAt timestamp`(): Unit = runTest {
        // GIVEN: Un objet avec un timestamp ancien (ex: 1000)
        val oldTimestamp = 1000L
        val item = CollectionItem(id = 1, titre = "Test", updatedAt = oldTimestamp)

        // WHEN: On l'insère via le repository
        repository.insert(item)

        // THEN : Le DAO doit recevoir un objet avec un timestamp récent (supérieur à l'ancien)
        verify(mockDao).insert(argThat { 
            this.updatedAt > oldTimestamp 
        })
    }

    @Test
    fun `update should refresh updatedAt timestamp`(): Unit = runTest {
        // GIVEN: Un objet existant avec un timestamp ancien
        val oldTimestamp = 1000L
        val item = CollectionItem(id = 1, titre = "Test", updatedAt = oldTimestamp)

        // WHEN: On le met à jour
        repository.update(item)

        // THEN: Le DAO doit recevoir l'objet avec un nouveau timestamp
        verify(mockDao).update(argThat { 
            this.updatedAt > oldTimestamp 
        })
    }
}
