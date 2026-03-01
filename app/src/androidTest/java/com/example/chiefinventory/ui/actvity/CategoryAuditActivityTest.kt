package com.example.chiefinventory.ui.actvity

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [CategoryAuditActivity].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CategoryAuditActivityTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var repository: CollectionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        repository = CollectionRepository(db.collectionDao())
        
        val app = context as CollectionApplication
        app.repository = repository
        app.locationRepository = LocationRepository(db.locationDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun analyzeAndFix_shouldUpdateItemsAndFinish(): Unit = runTest {
        // GIVEN: Un objet avec une Super-Catégorie erronée (#N/D). 
        // isPossessed = true par défaut, on ne le précise plus.
        val item = CollectionItem(
            id = 1, titre = "Spirou Test", categorie = "Travaux pour Spirou",
            superCategorie = "#N/D"
        )
        db.collectionDao().insert(item)

        // WHEN: On lance l'audit
        val scenario = ActivityScenario.launch(CategoryAuditActivity::class.java)
        onView(withId(R.id.btn_analyze_audit)).perform(click())

        // THEN: Le résumé affiche 1 objet trouvé
        onView(withId(R.id.cv_audit_summary)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_audit_summary)).check(matches(withText(containsString("1"))))

        // WHEN: On lance la réparation
        onView(withId(R.id.btn_fix_audit)).perform(click())

        // THEN: L'activité se ferme (succès)
        Thread.sleep(500)
        assertTrue(scenario.state == Lifecycle.State.DESTROYED)
    }

    @Test
    fun analyzeNoIssues_shouldShowNoIssuesText(): Unit = runTest {
        // GIVEN: Un objet déjà correctement classé. isPossessed = true par défaut.
        val item = CollectionItem(
            id = 1, titre = "Spirou OK", categorie = "Travaux pour Spirou",
            superCategorie = "Presse"
        )
        db.collectionDao().insert(item)

        // WHEN: On lance l'audit
        ActivityScenario.launch(CategoryAuditActivity::class.java)
        onView(withId(R.id.btn_analyze_audit)).perform(click())

        // THEN: Le message "aucune réparation nécessaire" s'affiche
        onView(withId(R.id.tv_no_issues)).check(matches(isDisplayed()))
        onView(withId(R.id.cv_audit_summary)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun analyzeSoughtItem_shouldIdentifyIssue(): Unit = runTest {
        // GIVEN: Un objet RECHERCHÉ (isPossessed = false) avec une erreur
        val item = CollectionItem(
            id = 2, titre = "Recherche Erronée", categorie = "Travaux pour Spirou",
            superCategorie = "N/D", isPossessed = false
        )
        db.collectionDao().insert(item)

        // WHEN: On lance l'audit
        ActivityScenario.launch(CategoryAuditActivity::class.java)
        onView(withId(R.id.btn_analyze_audit)).perform(click())

        // THEN: L'objet doit être identifié malgré le fait qu'il ne soit pas possédé
        onView(withId(R.id.cv_audit_summary)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_audit_summary)).check(matches(withText(containsString("1"))))
    }
}
