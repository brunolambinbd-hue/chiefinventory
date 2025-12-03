package com.example.parabdcollector.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
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
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [MainActivity] to verify dashboard information.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: CollectionDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
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
    fun dashboardCounters_shouldDisplayCorrectCounts() = runTest {
        // GIVEN: 3 possessed items and 2 sought items are inserted into the database.
        val possessedItems = (1..3).map {
            CollectionItem(id = it.toLong(), titre = "Possessed $it", editeur = "", annee = 2023, description = "", isPossessed = true, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null, remoteId = null)
        }
        val soughtItems = (4..5).map {
            CollectionItem(id = it.toLong(), titre = "Sought $it", editeur = "", annee = 2023, description = "", isPossessed = false, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "", prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null, remoteId = null)
        }
        (possessedItems + soughtItems).forEach { dao.insert(it) }

        // WHEN: The MainActivity is launched.
        ActivityScenario.launch(MainActivity::class.java)

        // THEN: The dashboard TextViews should reflect the correct counts, using string resources for i18n.
        val possessedText = context.getString(R.string.possessed_items_label, 3)
        val soughtText = context.getString(R.string.sought_items_label, 2)
        val totalText = context.getString(R.string.total_items_label, 5)

        onView(withId(R.id.possessed_items_text)).check(matches(withText(possessedText)))
        onView(withId(R.id.sought_items_text)).check(matches(withText(soughtText)))
        onView(withId(R.id.total_items_text)).check(matches(withText(totalText)))
    }
}
