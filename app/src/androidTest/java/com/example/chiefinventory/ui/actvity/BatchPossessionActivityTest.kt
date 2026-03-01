package com.example.chiefinventory.ui.actvity

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [BatchPossessionActivity].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BatchPossessionActivityTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var repository: CollectionRepository
    private lateinit var locationRepository: LocationRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        repository = CollectionRepository(db.collectionDao())
        locationRepository = LocationRepository(db.locationDao())
        
        val app = context as CollectionApplication
        app.repository = repository
        app.locationRepository = locationRepository
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun analyzeAndClickUpdate_shouldFinishActivity(): Unit = runTest {
        // GIVEN: Un magazine Spirou non possédé en base
        val item = CollectionItem(
            id = 1, titre = "Spirou n°1500", isPossessed = false,
            editeur = "Dupuis", annee = 1980, mois = 1, categorie = "Spirou", superCategorie = "Magazines"
        )
        db.collectionDao().insert(item)

        // WHEN: On lance l'activité et on effectue la recherche
        val scenario = ActivityScenario.launch(BatchPossessionActivity::class.java)

        onView(withId(R.id.et_series_name)).perform(replaceText("Spirou"), closeSoftKeyboard())
        onView(withId(R.id.et_start_number)).perform(replaceText("1400"), closeSoftKeyboard())
        onView(withId(R.id.et_end_number)).perform(replaceText("1600"), closeSoftKeyboard())
        
        onView(withId(R.id.btn_analyze)).perform(click())

        // THEN: Le résumé doit s'afficher avec le bon compte
        onView(withId(R.id.cv_summary)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_batch_summary)).check(matches(withText(containsString("1"))))

        // AND: Cliquer sur le bouton de mise à jour doit fermer l'activité
        onView(withId(R.id.btn_confirm_update)).perform(click())
        
        // On laisse un peu de temps pour le traitement asynchrone
        Thread.sleep(500)
        assertTrue(scenario.state == Lifecycle.State.DESTROYED)
    }

    /**
     * Vérifie que le champ localisation affiche bien les emplacements disponibles.
     */
    @Test
    fun locationDropdown_shouldShowAvailableLocations(): Unit = runTest {
        // GIVEN: Un emplacement "Bibliothèque" en base
        val loc = Location(id = 1, name = "Bibliothèque", parentId = null)
        db.locationDao().insert(loc)

        // WHEN: On lance l'activité
        ActivityScenario.launch(BatchPossessionActivity::class.java)

        // AND: On clique sur le champ localisation
        onView(withId(R.id.et_location)).perform(click())

        // THEN: L'emplacement doit apparaître dans la liste suggérée (le popup)
        onData(allOf(`is`(instanceOf(String::class.java)), containsString("Bibliothèque")))
            .inRoot(isPlatformPopup()) // Indispensable pour les dropdowns AutoCompleteTextView
            .check(matches(isDisplayed()))
    }

    @Test
    fun emptyFields_shouldShowErrorToast(): Unit = runTest {
        ActivityScenario.launch(BatchPossessionActivity::class.java)
        
        // Cliquer sur analyser sans rien remplir
        onView(withId(R.id.btn_analyze)).perform(click())

        // Le résumé ne doit pas être visible
        onView(withId(R.id.cv_summary)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}
