package com.example.parabdcollector.ui

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.dao.CollectionDao
import com.example.parabdcollector.dao.LocationDao
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.actvity.EditItemActivity
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI and integration tests for the [EditItemActivity].
 * This test class verifies the creation and editing of collection items.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class EditItemActivityTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var collectionDao: CollectionDao
    private lateinit var locationDao: LocationDao

    // Repositories
    private lateinit var collectionRepository: CollectionRepository
    private lateinit var locationRepository: LocationRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        collectionDao = db.collectionDao()
        locationDao = db.locationDao()

        collectionRepository = CollectionRepository(collectionDao)
        locationRepository = LocationRepository(locationDao)

        // Inject the test repositories into the application so the ViewModelFactory picks them up.
        val app = context as CollectionApplication
        app.repository = collectionRepository
        app.locationRepository = locationRepository
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Tests that creating a new item by entering a title and saving results
     * in the item being correctly inserted into the database.
     */
    @Test
    fun createNewItem_shouldSaveItemToDatabase() = runTest {
        val scenario = ActivityScenario.launch(EditItemActivity::class.java)

        val testTitle = "New Test Item from UI"
        onView(withId(R.id.etTitle)).perform(scrollTo(), replaceText(testTitle), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())

        delay(500)
        assertTrue("Activity should be destroyed after saving", scenario.state == Lifecycle.State.DESTROYED)

        // The `search` function only finds unpossessed items. To verify creation, we must
        // fetch all items and then find the one we just created.
        val allItems = collectionDao.getAllSuspend()
        val savedItem = allItems.find { it.titre == testTitle }

        assertNotNull("Item should be saved and found in DB", savedItem)
        assertEquals(testTitle, savedItem?.titre)
        assertTrue("Newly created item should be possessed by default", savedItem?.isPossessed == true)
    }

    /**
     * Tests that when editing an existing item, the data is loaded correctly,
     * changes are saved, and the original item is updated in the database.
     */
    @Test
    fun editExistingItem_shouldLoadData_and_SaveChanges() = runTest {
        val initialItem = CollectionItem(
            id = 1, remoteId = null, titre = "Titre Initial", editeur = "Editeur Initial", annee = 2020, description = "Desc Init",
            isPossessed = true, mois = 1, categorie = "Cat Init", superCategorie = "SuperCat Init", materiau = "", tirage = "", dimensions = "",
            prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null
        )
        collectionDao.insert(initialItem)

        val intent = Intent(ApplicationProvider.getApplicationContext(), EditItemActivity::class.java).apply {
            putExtra("itemId", 1L)
        }
        ActivityScenario.launch<EditItemActivity>(intent)

        onView(withId(R.id.etTitle)).check(matches(withText("Titre Initial")))
        onView(withId(R.id.etEditor)).check(matches(withText("Editeur Initial")))

        val updatedTitle = "Titre Mis à Jour"
        onView(withId(R.id.etTitle)).perform(scrollTo(), replaceText(updatedTitle), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())
        
        delay(500) 

        val updatedItemInDb = collectionDao.getItemById(1)
        assertNotNull("Item should be found in DB after update", updatedItemInDb)
        assertEquals(updatedTitle, updatedItemInDb?.titre)
        assertEquals(1L, updatedItemInDb?.id)
        assertEquals("Editeur Initial", updatedItemInDb?.editeur)
    }
}
