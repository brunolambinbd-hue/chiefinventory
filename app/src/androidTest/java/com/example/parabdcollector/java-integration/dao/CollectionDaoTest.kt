package com.example.parabdcollector.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.utils.getOrAwaitValue
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
        id = 0,
        titre = "Default Title",
        editeur = "Default Editor",
        annee = 2000,
        mois = 1,
        categorie = "Spirou",
        superCategorie = "Magazines",
        isPossessed = true
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
        val item = baseItem.copy(id = 1, titre = "Le Test")
        collectionDao.insert(item)
        val retrievedItem = collectionDao.getItemById(1)
        assertThat(retrievedItem).isEqualTo(item)
    }

    /**
     * Vérifie que l'ordre d'affichage (Année DESC, Mois DESC) est respecté pour les catégories.
     */
    @Test
    fun getItemsBySuperCategoryAndCategory_returnsItemsInCorrectOrder() = runTest {
        // GIVEN: 4 objets de la même catégorie avec des dates différentes
        val itemOld = baseItem.copy(id = 1, titre = "Ancien", annee = 1980, mois = 12)
        val itemNewYear = baseItem.copy(id = 2, titre = "Nouveau Année", annee = 2024, mois = 1)
        val itemMidMonth1 = baseItem.copy(id = 3, titre = "Moyen Mois 5", annee = 2000, mois = 5)
        val itemMidMonth2 = baseItem.copy(id = 4, titre = "Moyen Mois 10", annee = 2000, mois = 10)

        collectionDao.insert(itemOld)
        collectionDao.insert(itemNewYear)
        collectionDao.insert(itemMidMonth1)
        collectionDao.insert(itemMidMonth2)

        // WHEN: On récupère les objets de la catégorie "Spirou"
        val results = collectionDao.getItemsBySuperCategoryAndCategory("Magazines", "Spirou", true).getOrAwaitValue()

        // THEN: L'ordre doit être : 2024/01 -> 2000/10 -> 2000/05 -> 1980/12
        assertThat(results).hasSize(4)
        assertThat(results).containsExactly(itemNewYear, itemMidMonth2, itemMidMonth1, itemOld).inOrder()
    }

    @Test
    fun searchItems_returnsItems_inCorrectOrder() = runTest {
        val item1 = baseItem.copy(id = 1, titre = "Blueberry 1", editeur = "Dargaud", annee = 1980)
        val item2 = baseItem.copy(id = 2, titre = "Blueberry 2", editeur = "Dargaud", annee = 1981)
        collectionDao.insert(item1)
        collectionDao.insert(item2)

        val searchResults = collectionDao.search("%Dargaud%")
        assertThat(searchResults).containsExactly(item2, item1).inOrder()
    }

    @Test
    fun getUnlocatedItems_logic() = runTest {
        val unlocated = baseItem.copy(id = 1, locationId = null, isPossessed = true)
        val located = baseItem.copy(id = 2, locationId = 100, isPossessed = true)
        collectionDao.insert(unlocated)
        collectionDao.insert(located)

        val results = collectionDao.getUnlocatedItems().getOrAwaitValue()
        assertThat(results).containsExactly(unlocated)
    }
}
