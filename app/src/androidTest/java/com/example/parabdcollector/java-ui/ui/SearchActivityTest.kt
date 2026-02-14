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
import com.example.parabdcollector.model.AdvancedSearchResult
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.SearchCriteria
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.ui.actvity.SearchActivity
import com.example.parabdcollector.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    /**
     * Test specifically verifying that image search works with both
     * FLOAT32 and Quantized signatures.
     */
    @Test
    fun imageSearch_shouldHandleBothSignatureFormats() = runTest {
        // 1. GIVEN: Two items, one with FLOAT32 signature, one with Quantized signature
        // Using 4-dimensional embeddings for simplicity in test
        val floatSignature = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
        val floatBytes = ByteBuffer.allocate(floatSignature.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { for (f in floatSignature) putFloat(f) }
            .array()

        val quantizedBytes = byteArrayOf(127, 0, 0, 0) // Max value at first index, similar to floatSignature

        val itemFloat = CollectionItem(
            id = 10, titre = "Float Item", imageEmbedding = floatBytes,
            isPossessed = true, editeur = "", annee = 2023, description = ""
        )
        val itemQuant = CollectionItem(
            id = 11, titre = "Quant Item", imageEmbedding = quantizedBytes,
            isPossessed = true, editeur = "", annee = 2023, description = ""
        )

        dao.insert(itemFloat)
        dao.insert(itemQuant)

        // 2. Mocking the repository to simulate an image search with a similar float array
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as CollectionApplication
        val mockRepo = object : CollectionRepository(dao) {
            override suspend fun advancedSearch(cr: SearchCriteria, qE: FloatArray?): AdvancedSearchResult {
                // We delegate to the super class to test the actual similarity logic we modified
                return super.advancedSearch(cr, qE)
            }
        }
        app.repository = mockRepo

        // 3. WHEN & THEN: Verify the logic by directly calling the repo (instrumented test of the logic)
        // Image to search: [1.0, 0, 0, 0]
        val searchEmbedding = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)

        val result = mockRepo.advancedSearch(SearchCriteria(), searchEmbedding)

        // Both should be found because the logic now handles both formats
        val foundTitles = result.results.map { it.item.titre }
        assertTrue("Float item should be found", foundTitles.contains("Float Item"))
        assertTrue("Quant item should be found", foundTitles.contains("Quant Item"))
    }

    /**
     * Test verifying the comparison between a "downloaded" image signature
     * and a "scanned" image signature using cosine similarity.
     */
    @Test
    fun imageSearch_shouldFindDownloadedItem_whenSearchingWithSimilarSignature() = runTest {
        // 1. GIVEN: An item representing one downloaded from a site with its signature.
        // We simulate a 128-dim vector where values are slightly noisy.
        val downloadedSignature = FloatArray(128) { 0.1f }
        downloadedSignature[0] = 0.9f // Key feature
        
        val floatBytes = ByteBuffer.allocate(downloadedSignature.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { for (f in downloadedSignature) putFloat(f) }
            .array()

        val importedItem = CollectionItem(
            id = 100,
            titre = "Imported BD from Website",
            imageEmbedding = floatBytes,
            isPossessed = true,
            editeur = "Frank Pé",
            annee = 2024,
            description = "Imported via CSV"
        )
        dao.insert(importedItem)

        // 2. WHEN: We search with a "scanned" signature (simulated here) that is very similar.
        val scannedSignature = FloatArray(128) { 0.11f }
        scannedSignature[0] = 0.85f // Close to 0.9 but with some "sensor noise"
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as CollectionApplication
        
        // Use the real repository logic to verify the similarity threshold
        val results = app.repository.advancedSearch(SearchCriteria(), scannedSignature)

        // 3. THEN: The item should be found.
        val matchingResult = results.results.find { it.item.titre == "Imported BD from Website" }
        assertNotNull("The imported item should be in the search results", matchingResult)
        
        val similarity = matchingResult?.similarity ?: 0.0
        assertTrue("Similarity ($similarity) should be above the 0.65 threshold", similarity >= 0.65)
        // With these values, cosine similarity should be quite high (~0.9+)
        assertTrue("Similarity ($similarity) should be high for very close vectors", similarity > 0.9)
    }
}
