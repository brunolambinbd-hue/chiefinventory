package com.example.chiefinventory.java_integration.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.java_integration.utils.getOrAwaitValue
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
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var collectionDao: CollectionDao

    private val baseItem = CollectionItem(
        titre = "Default Title",
        editeur = "Default Editor",
        annee = 2000,
        mois = 1,
        categorie = "Spirou",
        superCategorie = "Magazines"
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
    fun insertItemAndGetById(): Unit = runTest {
        val item = baseItem.copy(id = 1, titre = "Le Test")
        collectionDao.insert(item)
        val retrievedItem = collectionDao.getItemById(1)
        assertThat(retrievedItem).isEqualTo(item)
    }

    @Test
    fun getAllSought_returnsOnlyNonPossessedItems(): Unit = runTest {
        val possessed = baseItem.copy(id = 1, titre = "Possédé", isPossessed = true)
        val sought = baseItem.copy(id = 2, titre = "Recherché", isPossessed = false)
        collectionDao.insert(possessed)
        collectionDao.insert(sought)

        val results = collectionDao.getAllSought().getOrAwaitValue()
        assertThat(results).hasSize(1)
        assertThat(results[0].titre).isEqualTo("Recherché")
    }

    /**
     * Vérifie que l'ordre d'affichage (Année DESC, Mois DESC) est respecté pour les catégories.
     */
    @Test
    fun getItemsBySuperCategoryAndCategory_returnsItemsInCorrectOrder(): Unit = runTest {
        val itemOld = baseItem.copy(id = 1, titre = "Ancien", annee = 1980, mois = 12)
        val itemNewYear = baseItem.copy(id = 2, titre = "Nouveau Année", annee = 2024, mois = 1)
        val itemMidMonth1 = baseItem.copy(id = 3, titre = "Moyen Mois 5", annee = 2000, mois = 5)
        val itemMidMonth2 = baseItem.copy(id = 4, titre = "Moyen Mois 10", annee = 2000, mois = 10)

        collectionDao.insert(itemOld)
        collectionDao.insert(itemNewYear)
        collectionDao.insert(itemMidMonth1)
        collectionDao.insert(itemMidMonth2)

        val results = collectionDao.getItemsBySuperCategoryAndCategory("Magazines", "Spirou", true).getOrAwaitValue()

        assertThat(results).hasSize(4)
        assertThat(results).containsExactly(itemNewYear, itemMidMonth2, itemMidMonth1, itemOld).inOrder()
    }

    @Test
    fun searchItems_returnsItems_inCorrectOrder(): Unit = runTest {
        val item1 = baseItem.copy(id = 1, titre = "Blueberry 1", editeur = "Dargaud", annee = 1980)
        val item2 = baseItem.copy(id = 2, titre = "Blueberry 2", editeur = "Dargaud", annee = 1981)
        collectionDao.insert(item1)
        collectionDao.insert(item2)

        val searchResults = collectionDao.search("%Dargaud%")
        assertThat(searchResults).containsExactly(item2, item1).inOrder()
    }

    @Test
    fun getUnlocatedItems_logic(): Unit = runTest {
        val unlocated = baseItem.copy(id = 1, locationId = null, isPossessed = true)
        val located = baseItem.copy(id = 2, locationId = 100, isPossessed = true)
        collectionDao.insert(unlocated)
        collectionDao.insert(located)

        val results = collectionDao.getUnlocatedItems().getOrAwaitValue()
        assertThat(results).containsExactly(unlocated)
    }

    @Test
    fun getLocatedNotPossessedItems_logic(): Unit = runTest {
        val target = baseItem.copy(id = 1, locationId = 50, isPossessed = false)
        val other1 = baseItem.copy(id = 2, locationId = null, isPossessed = false)
        val other2 = baseItem.copy(id = 3, locationId = 50, isPossessed = true)
        
        collectionDao.insert(target)
        collectionDao.insert(other1)
        collectionDao.insert(other2)

        val results = collectionDao.getLocatedNotPossessedItems().getOrAwaitValue()
        assertThat(results).containsExactly(target)
    }
}
