package com.example.chiefinventory.java_ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.actvity.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Stable Instrumented UI tests for [MainActivity].
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        
        // Setup in-memory database
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Inject repositories into the Application container
        val app = context as CollectionApplication
        app.repository = CollectionRepository(db.collectionDao())
        app.locationRepository = LocationRepository(db.locationDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun dashboardCounters_shouldDisplayCorrectCounts() {
        // GIVEN: Populate data before Activity launch
        runBlocking {
            val dao = db.collectionDao()
            dao.insert(CollectionItem(id = 1, titre = "P1", isPossessed = true))
            dao.insert(CollectionItem(id = 2, titre = "P2", isPossessed = true))
            dao.insert(CollectionItem(id = 3, titre = "P3", isPossessed = true))
            dao.insert(CollectionItem(id = 4, titre = "S1", isPossessed = false))
            dao.insert(CollectionItem(id = 5, titre = "S2", isPossessed = false))
        }

        // WHEN: Launch activity. Espresso handles waiting for Idle state.
        ActivityScenario.launch(MainActivity::class.java).use {
            val possessedText = context.getString(R.string.possessed_items_label, 3)
            val soughtText = context.getString(R.string.sought_items_label, 2)
            val totalText = context.getString(R.string.total_items_label, 5)

            // THEN: Verification
            onView(withId(R.id.possessed_items_text)).check(matches(withText(possessedText)))
            onView(withId(R.id.sought_items_text)).check(matches(withText(soughtText)))
            onView(withId(R.id.total_items_text)).check(matches(withText(totalText)))
        }
    }
}
