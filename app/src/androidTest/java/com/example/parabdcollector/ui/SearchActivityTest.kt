package com.example.parabdcollector.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
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
import com.example.parabdcollector.ui.actvity.SearchActivity
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI and integration tests for the [SearchActivity].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SearchActivityTest {

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
    fun simpleSearch_shouldDisplayCorrectResults() = runTest {
        // GIVEN: An item with a unique title is inserted into the database.
        val uniqueTitle = "Objet de Test pour Recherche"
        val testItem = CollectionItem(
            id = 1, titre = uniqueTitle, editeur = "", annee = 2023, description = "",
            isPossessed = false, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "",
            prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null, remoteId = null
        )
        dao.insert(testItem)

        // WHEN: The SearchActivity is launched, the user types the unique title and clicks search.
        ActivityScenario.launch(SearchActivity::class.java)

        onView(withId(R.id.et_search_simple)).perform(replaceText(uniqueTitle), closeSoftKeyboard())
        onView(withId(R.id.btn_search)).perform(click())

        // THEN: The item with the unique title should be displayed in the results list.
        onView(allOf(withText(uniqueTitle), isDescendantOfA(withId(R.id.rv_search_results))))
            .check(matches(isDisplayed()))
    }

    @Test
    fun advancedSearch_shouldDisplayCorrectResults() = runTest {
        // GIVEN: Two items, one of which will match the search criteria.
        val matchingItem = CollectionItem(
            id = 1, titre = "Matching Item", editeur = "Matching Editor", annee = 2023, description = "",
            isPossessed = false, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "",
            prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null, remoteId = null
        )
        val nonMatchingItem = CollectionItem(
            id = 2, titre = "Non-Matching Item", editeur = "Non-Matching Editor", annee = 2020, description = "",
            isPossessed = false, mois = 1, categorie = "", superCategorie = "", materiau = "", tirage = "", dimensions = "",
            prixAchat = 0.0, valeurEstimee = 0.0, lieuAchat = "", imageUri = "", imageEmbedding = null, locationId = null, remoteId = null
        )
        dao.insert(matchingItem)
        dao.insert(nonMatchingItem)

        // WHEN: The activity is launched, advanced search is opened, criteria entered, and search is clicked.
        ActivityScenario.launch(SearchActivity::class.java)

        onView(withId(R.id.tv_toggle_advanced_search)).perform(click())
        onView(withId(R.id.et_search_editor)).perform(replaceText("Matching Editor"), closeSoftKeyboard())
        onView(withId(R.id.et_search_year)).perform(replaceText("2023"), closeSoftKeyboard())
        onView(withId(R.id.btn_search)).perform(click())

        // THEN: The matching item should be displayed, and the non-matching item should not.
        onView(allOf(withText("Matching Item"), isDescendantOfA(withId(R.id.rv_search_results))))
            .check(matches(isDisplayed()))
        onView(allOf(withText("Non-Matching Item"), isDescendantOfA(withId(R.id.rv_search_results))))
            .check(doesNotExist())
    }

//    @Test
//    fun searchError_navigatesToCrashActivity() {
//        // GIVEN a repository that will always throw an unhandled error
//        val errorRepository = object : CollectionRepository(dao) {
//            override suspend fun search(query: String): List<CollectionItem> {
//                // Use a non-Exception Throwable to bypass the ViewModel's catch block
//                // and trigger the GlobalExceptionHandler.
//                throw Error("Simulated fatal database error")
//            }
//        }
//        val app = ApplicationProvider.getApplicationContext<CollectionApplication>()
//        app.repository = errorRepository
//
//        // WHEN the activity is launched and a search is performed that will crash
//        ActivityScenario.launch(SearchActivity::class.java)
//        onView(withId(R.id.et_search_simple)).perform(replaceText("any query"), closeSoftKeyboard())
//        onView(withId(R.id.btn_search)).perform(click())
//
//        // THEN the CrashActivity should be displayed
//        onView(withId(R.id.crash_activity_root)).check(matches(isDisplayed()))
//        onView(withId(R.id.tv_crash_title)).check(matches(withText(R.string.crash_title)))
//    }
}
