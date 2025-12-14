package com.example.parabdcollector.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class CollectionDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var collectionDao: CollectionDao

    private val baseItem = CollectionItem(
        id = 0, // id will be auto-generated
        remoteId = null,
        titre = "Default Title",
        editeur = "Default Editor",
        annee = 2000,
        mois = 1,
        categorie = "Default Category",
        superCategorie = "Default Super Category",
        materiau = null, tirage = null, dimensions = null, prixAchat = null, 
        valeurEstimee = null, lieuAchat = null, description = null, imageUri = null,
        imageEmbedding = null, locationId = null, isPossessed = true
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        collectionDao = database.collectionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertItemAndGetById() = runTest {
        val item = baseItem.copy(id = 1, titre = "Le Test de la Licorne")
        collectionDao.insert(item)

        val retrievedItem = collectionDao.getItemById(1)

        assertThat(retrievedItem).isNotNull()
        assertThat(retrievedItem).isEqualTo(item)
    }

    @Test
    fun updateItemAndCheck() = runTest {
        val originalItem = baseItem.copy(id = 1, titre = "Titre Original")
        collectionDao.insert(originalItem)

        val updatedItem = originalItem.copy(titre = "Titre Modifié", annee = 2022)
        collectionDao.update(updatedItem)

        val retrievedItem = collectionDao.getItemById(1)
        assertThat(retrievedItem).isNotNull()
        assertThat(retrievedItem?.titre).isEqualTo("Titre Modifié")
        assertThat(retrievedItem?.annee).isEqualTo(2022)
    }

    @Test
    fun deleteItemAndVerifyAbsence() = runTest {
        val item = baseItem.copy(id = 1, titre = "Item à supprimer")
        collectionDao.insert(item)

        assertThat(collectionDao.getItemById(1)).isNotNull()

        collectionDao.delete(item)

        val retrievedItem = collectionDao.getItemById(1)
        assertThat(retrievedItem).isNull()
    }

    @Test
    fun searchItems_returnsMatchingUnpossessedItems_inCorrectOrder() = runTest {
        // Arrange: Insert a variety of items
        val item1 = baseItem.copy(id = 1, titre = "Blueberry 1", editeur = "Dargaud", annee = 1980, isPossessed = false)
        val item2 = baseItem.copy(id = 2, titre = "Thorgal 5", editeur = "Lombard", annee = 1982, isPossessed = false)
        val item3 = baseItem.copy(id = 3, titre = "Blueberry 2", editeur = "Dargaud", annee = 1981, isPossessed = true) // Possessed, should not be found
        val item4 = baseItem.copy(id = 4, titre = "XIII 1", editeur = "Dargaud", annee = 1984, isPossessed = false)

        collectionDao.insert(item1)
        collectionDao.insert(item2)
        collectionDao.insert(item3)
        collectionDao.insert(item4)

        // Act: Perform a search for an editor
        val searchResults = collectionDao.search("%Dargaud%")

        // Assert: Check the results
        assertThat(searchResults).hasSize(2)
        // Verify that the order is descending by year
        assertThat(searchResults).containsExactly(item4, item1).inOrder()
        assertThat(searchResults).doesNotContain(item2)
        assertThat(searchResults).doesNotContain(item3)
    }
}