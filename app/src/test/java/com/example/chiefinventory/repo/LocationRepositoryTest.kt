package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.model.Location
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for the [LocationRepository].
 *
 * This class uses Mockito to create a mock [LocationDao] to test the repository's logic
 * in isolation from the actual database.
 */
class LocationRepositoryTest {

    /**
     * This rule makes sure that LiveData updates happen synchronously in tests.
     */
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    // The mock DAO that will be used in the tests.
    private lateinit var locationDao: LocationDao
    // The repository instance under test.
    private lateinit var locationRepository: LocationRepository

    /**
     * Sets up the test environment before each test.
     * This creates a new mock DAO and a new repository instance.
     */
    @Before
    fun setup() {
        locationDao = mock()
        locationRepository = LocationRepository(locationDao)
    }

    /**
     * Verifies that calling [LocationRepository.getAll] correctly calls the
     * corresponding method on the DAO and returns the expected data.
     */
    @Test
    fun `getAll should return all locations from dao`() {
        // GIVEN: A LiveData object that will be returned by the mock DAO.
        val liveData = MutableLiveData<List<Location>>()
        val testData = listOf(Location(id = 1, name = "Salon", parentId = null))
        liveData.value = testData
        whenever(locationDao.getAll()).thenReturn(liveData)

        // WHEN: The getAll method is called on the repository.
        val result = locationRepository.getAll()

        // THEN: The result should be the same LiveData object provided by the DAO.
        assertEquals(liveData, result)
        assertEquals(testData, result.value)
    }

    /**
     * Verifies that calling [LocationRepository.insert] correctly calls the
     * corresponding method on the DAO.
     */
    @Test
    fun `insert should call insert on dao`(): Unit = runBlocking {
        // GIVEN: A location object to insert.
        val location = Location(name = "Cuisine", parentId = null)

        // WHEN: The insert method is called on the repository.
        locationRepository.insert(location)

        // THEN: The insert method on the DAO should be called with the same object.
        verify(locationDao).insert(location)
    }

    /**
     * Verifies that calling [LocationRepository.update] correctly calls the
     * corresponding method on the DAO.
     */
    @Test
    fun `update should call update on dao`(): Unit = runBlocking {
        // GIVEN: A location object to update.
        val location = Location(id = 1, name = "Salon V2", parentId = null)

        // WHEN: The update method is called on the repository.
        locationRepository.update(location)

        // THEN: The update method on the DAO should be called with the same object.
        verify(locationDao).update(location)
    }

    /**
     * Verifies that calling [LocationRepository.delete] correctly calls the
     * corresponding method on the DAO.
     */
    @Test
    fun `delete should call delete on dao`(): Unit = runBlocking {
        // GIVEN: A location object to delete.
        val location = Location(id = 1, name = "Salon", parentId = null)

        // WHEN: The delete method is called on the repository.
        locationRepository.delete(location)

        // THEN: The delete method on the DAO should be called with the same object.
        verify(locationDao).delete(location)
    }
}
