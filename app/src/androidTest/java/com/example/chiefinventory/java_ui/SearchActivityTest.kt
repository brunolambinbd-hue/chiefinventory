package com.example.chiefinventory.java_ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.AdvancedSearchResult
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.ui.actvity.SearchActivity
import com.example.chiefinventory.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Instrumented UI and integration tests for the [com.example.chiefinventory.ui.actvity.SearchActivity].
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SearchActivityTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

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
    fun simpleSearch_shouldDisplayCorrectResults(): Unit = runTest {
        // GIVEN: An item with a unique title is inserted into the database.
        val uniqueTitle = "Objet de Test pour Recherche"
        val testItem = CollectionItem(
            id = 1,
            titre = uniqueTitle,
            editeur = "",
            annee = 2023,
            description = "",
            isPossessed = false,
            mois = 1,
            categorie = "",
            superCategorie = "",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = ""
        )
        dao.insert(testItem)

        // WHEN: The SearchActivity is launched, the user types the unique title and clicks search.
        ActivityScenario.launch(SearchActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.et_search_simple))
            .perform(ViewActions.replaceText(uniqueTitle), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.btn_search)).perform(ViewActions.click())

        // THEN: The item with the unique title should be displayed in the results list.
        // We use scrollTo() because results might be below the keyboard or fold.
        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withText(uniqueTitle),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.rv_search_results))
            )
        )
            .perform(ViewActions.scrollTo())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun advancedSearch_shouldDisplayCorrectResults(): Unit = runTest {
        // GIVEN: Two items, one of which will match the search criteria.
        val matchingItem = CollectionItem(
            id = 1,
            titre = "Matching Item",
            editeur = "Matching Editor",
            annee = 2023,
            description = "",
            isPossessed = false,
            mois = 1,
            categorie = "",
            superCategorie = "",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = ""
        )
        val nonMatchingItem = CollectionItem(
            id = 2,
            titre = "Non-Matching Item",
            editeur = "Non-Matching Editor",
            annee = 2020,
            description = "",
            isPossessed = false,
            mois = 1,
            categorie = "",
            superCategorie = "",
            materiau = "",
            tirage = "",
            dimensions = "",
            prixAchat = 0.0,
            valeurEstimee = 0.0,
            lieuAchat = "",
            imageUri = ""
        )
        dao.insert(matchingItem)
        dao.insert(nonMatchingItem)

        // WHEN: The activity is launched, advanced search is opened, criteria entered, and search is clicked.
        ActivityScenario.launch(SearchActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.tv_toggle_advanced_search)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.et_search_editor))
            .perform(ViewActions.replaceText("Matching Editor"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.et_search_year))
            .perform(ViewActions.replaceText("2023"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.btn_search)).perform(ViewActions.click())

        // THEN: The matching item should be displayed, and the non-matching item should not.
        // Important: Use scrollTo() as the advanced search fields push results off-screen.
        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withText("Matching Item"),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.rv_search_results))
            )
        )
            .perform(ViewActions.scrollTo())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withText("Non-Matching Item"),
                ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.rv_search_results))
            )
        )
            .check(ViewAssertions.doesNotExist())
    }

    /**
     * Test specifically verifying that image search works with both
     * FLOAT32 and Quantized signatures.
     */
    @Test
    fun imageSearch_shouldHandleBothSignatureFormats(): Unit = runTest {
        // 1. GIVEN: Two items, one with FLOAT32 signature, one with Quantized signature
        val floatSignature = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
        val floatBytes = ByteBuffer.allocate(floatSignature.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { for (f in floatSignature) putFloat(f) }
            .array()

        val quantizedBytes = byteArrayOf(127, 0, 0, 0)

        val itemFloat = CollectionItem(
            id = 10, titre = "Float Item", imageEmbedding = floatBytes,
            editeur = "", annee = 2023, description = ""
        )
        val itemQuant = CollectionItem(
            id = 11, titre = "Quant Item", imageEmbedding = quantizedBytes,
            editeur = "", annee = 2023, description = ""
        )

        dao.insert(itemFloat)
        dao.insert(itemQuant)

        // 2. Mocking the repository to simulate an image search
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as CollectionApplication
        val mockRepo = object : CollectionRepository(dao) {
            override suspend fun advancedSearch(cr: SearchCriteria, qE: FloatArray?, qOcr: String?): AdvancedSearchResult {
                return super.advancedSearch(cr, qE, qOcr)
            }
        }
        app.repository = mockRepo

        // 3. WHEN & THEN
        val searchEmbedding = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
        val result = mockRepo.advancedSearch(SearchCriteria(), searchEmbedding)

        val foundTitles = result.results.map { it.item.titre }
        Assert.assertTrue("Float item should be found", foundTitles.contains("Float Item"))
        Assert.assertTrue("Quant item should be found", foundTitles.contains("Quant Item"))
    }

    /**
     * Test verifying the comparison between a "downloaded" image signature
     * and a "scanned" image signature using cosine similarity.
     */
    @Test
    fun imageSearch_shouldFindDownloadedItem_whenSearchingWithSimilarSignature(): Unit = runTest {
        // 1. GIVEN: An item representing one downloaded from a site with its signature.
        val downloadedSignature = FloatArray(128) { 0.1f }
        downloadedSignature[0] = 0.9f 
        
        val floatBytes = ByteBuffer.allocate(downloadedSignature.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { for (f in downloadedSignature) putFloat(f) }
            .array()

        val importedItem = CollectionItem(
            id = 100, titre = "Imported BD from Website", imageEmbedding = floatBytes,
            editeur = "Frank Pé", annee = 2024, description = "Imported via CSV"
        )
        dao.insert(importedItem)

        // 2. WHEN: We search with a "scanned" signature
        val scannedSignature = FloatArray(128) { 0.11f }
        scannedSignature[0] = 0.85f 
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as CollectionApplication
        val results = app.repository.advancedSearch(SearchCriteria(), scannedSignature)

        // 3. THEN
        val matchingResult = results.results.find { it.item.titre == "Imported BD from Website" }
        Assert.assertNotNull("The imported item should be in the search results", matchingResult)
        Assert.assertTrue("Similarity should be above threshold", (matchingResult?.similarity ?: 0.0) >= 0.65)
    }
}
