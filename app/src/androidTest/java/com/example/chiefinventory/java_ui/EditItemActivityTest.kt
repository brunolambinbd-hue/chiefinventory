package com.example.chiefinventory.java_ui

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.dao.CollectionDao
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.actvity.EditItemActivity
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI and integration tests for the [com.example.chiefinventory.ui.actvity.EditItemActivity].
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
    fun createNewItem_shouldSaveItemToDatabase(): Unit = runTest {
        val scenario = ActivityScenario.launch(EditItemActivity::class.java)

        val testTitle = "New Test Item from UI"
        Espresso.onView(ViewMatchers.withId(R.id.etTitle)).perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText(testTitle),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.btnSave)).perform(ViewActions.click())

        delay(500)
        Assert.assertTrue(
            "Activity should be destroyed after saving",
            scenario.state == Lifecycle.State.DESTROYED
        )

        // The `search` function only finds unpossessed items. To verify creation, we must
        // fetch all items and then find the one we just created.
        val allItems = collectionDao.getAllSuspend()
        val savedItem = allItems.find { it.titre == testTitle }

        Assert.assertNotNull("Item should be saved and found in DB", savedItem)
        Assert.assertEquals(testTitle, savedItem?.titre)
        Assert.assertTrue(
            "Newly created item should be possessed by default",
            savedItem?.isPossessed == true
        )
    }

    /**
     * Tests that when editing an existing item, the data is loaded correctly,
     * changes are saved, and the original item is updated in the database.
     */
    @Test
    fun editExistingItem_shouldLoadData_and_SaveChanges(): Unit = runTest {
        val initialItem = CollectionItem(
            id = 1,
            remoteId = null,
            titre = "Titre Initial",
            editeur = "Editeur Initial",
            annee = 2020,
            description = "Desc Init",
            isPossessed = true,
            mois = 1,
            categorie = "Cat Init",
            superCategorie = "SuperCat Init",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = "",
            imageEmbedding = null,
            locationId = null
        )
        collectionDao.insert(initialItem)

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            EditItemActivity::class.java
        ).apply {
            putExtra("itemId", 1L)
        }
        ActivityScenario.launch<EditItemActivity>(intent)

        Espresso.onView(ViewMatchers.withId(R.id.etTitle))
            .check(ViewAssertions.matches(ViewMatchers.withText("Titre Initial")))
        Espresso.onView(ViewMatchers.withId(R.id.etEditor))
            .check(ViewAssertions.matches(ViewMatchers.withText("Editeur Initial")))

        val updatedTitle = "Titre Mis à Jour"
        Espresso.onView(ViewMatchers.withId(R.id.etTitle)).perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText(updatedTitle),
            ViewActions.closeSoftKeyboard()
        )
        Espresso.onView(ViewMatchers.withId(R.id.btnSave)).perform(ViewActions.click())

        delay(500)

        val updatedItemInDb = collectionDao.getItemById(1)
        Assert.assertNotNull("Item should be found in DB after update", updatedItemInDb)
        Assert.assertEquals(updatedTitle, updatedItemInDb?.titre)
        Assert.assertEquals(1L, updatedItemInDb?.id)
        Assert.assertEquals("Editeur Initial", updatedItemInDb?.editeur)
    }
}
