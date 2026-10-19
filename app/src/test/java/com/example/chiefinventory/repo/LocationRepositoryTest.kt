package com.example.chiefinventory.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.model.Location
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocationRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var locationDao: LocationDao
    private lateinit var locationRepository: LocationRepository

    @Before
    fun setup() {
        locationDao = mock()
        locationRepository = LocationRepository(locationDao)
    }

    @Test
    fun `getAll should return all locations from dao`() {
        val liveData = MutableLiveData<List<Location>>()
        val testData = listOf(Location(id = 1, name = "Salon", parentId = null))
        liveData.value = testData
        whenever(locationDao.getAll()).thenReturn(liveData)

        val result = locationRepository.getAll()

        assertThat(result).isEqualTo(liveData)
        assertThat(result.value).isEqualTo(testData)
    }

    @Test
    fun `insert should call insert on dao`(): Unit = runBlocking {
        val location = Location(name = "Cuisine", parentId = null)

        locationRepository.insert(location)

        verify(locationDao).insert(location)
    }

    @Test
    fun `update should call update on dao`(): Unit = runBlocking {
        val location = Location(id = 1, name = "Salon V2", parentId = null)

        locationRepository.update(location)

        verify(locationDao).update(location)
    }

    @Test
    fun `delete should call delete on dao`(): Unit = runBlocking {
        val location = Location(id = 1, name = "Salon", parentId = null)

        locationRepository.delete(location)

        verify(locationDao).delete(location)
    }
}
