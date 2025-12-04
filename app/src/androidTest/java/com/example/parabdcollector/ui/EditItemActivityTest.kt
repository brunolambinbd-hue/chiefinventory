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
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.actvity.EditItemActivity
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class EditItemActivityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: CollectionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.collectionDao()

        // Inject the test repository into the application.
        val app = context as CollectionApplication
        app.repository = CollectionRepository(dao)
        app.locationRepository = LocationRepository(db.locationDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createNewItem_shouldSaveItemToDatabase() = runTest {
        // GIVEN: The EditItemActivity is launched in 'new item' mode.
        val scenario = ActivityScenario.launch(EditItemActivity::class.java)

        // WHEN: The user fills a field and clicks the save button.
        val testTitle = "Titre UI Complet"
        onView(withId(R.id.etTitle)).perform(scrollTo(), replaceText(testTitle), closeSoftKeyboard())

        onView(withId(R.id.btnSave)).perform(click())

        // THEN: The activity should finish and the item should be in the database.
        Thread.sleep(500) // Give time for activity to close
        assertTrue(scenario.state == Lifecycle.State.DESTROYED)

        val savedItem = dao.search(testTitle).firstOrNull()
        assertNotNull("Item should be saved and found in DB", savedItem)
        assertEquals(testTitle, savedItem?.titre)
    }

    @Test
    fun editExistingItem_shouldLoadData_and_SaveChanges() = runTest {
        // GIVEN: An item is pre-inserted in the database with all required fields.
        val initialItem = CollectionItem(
            id = 1, remoteId = null, titre = "Titre Initial", editeur = "Editeur Initial", annee = 2020, description = "Desc Init",
            isPossessed = true, mois = 1, categorie = "Cat Init", superCategorie = "SuperCat Init", materiau = "", tirage = "", dimensions = "",
            prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null
        )
        dao.insert(initialItem)

        // AND: The activity is launched with the item's ID.
        val intent = Intent(ApplicationProvider.getApplicationContext(), EditItemActivity::class.java).apply {
            putExtra("itemId", 1L)
        }
        ActivityScenario.launch<EditItemActivity>(intent)

        // THEN: The UI should be pre-filled with the item's data.
        onView(withId(R.id.etTitle)).check(matches(withText("Titre Initial")))
        onView(withId(R.id.etEditor)).check(matches(withText("Editeur Initial")))

        // WHEN: The user edits a field and saves.
        val updatedTitle = "Titre Mis à Jour"
        onView(withId(R.id.etTitle)).perform(scrollTo(), replaceText(updatedTitle), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())

        // THEN: The changes should be saved in the database.
        Thread.sleep(500) // Give time for activity to close and DB to update
        val updatedItemInDb = dao.search(updatedTitle).firstOrNull()
        assertNotNull("Item should be found in DB after update", updatedItemInDb)
        assertEquals(updatedTitle, updatedItemInDb?.titre)
        assertEquals(1L, updatedItemInDb?.id) // Check ID is preserved
    }
}
