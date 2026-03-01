package com.example.chiefinventory.java_integration.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.java_integration.utils.getOrAwaitValue
import com.example.chiefinventory.model.Location
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class LocationDaoTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var locationDao: LocationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        locationDao = database.locationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertLocationAndReadIt(): Unit = runTest {
        val location = Location(id = 1, name = "Living Room Shelf", parentId = null)
        locationDao.insert(location)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).contains(location)
    }

    @Test
    fun updateLocationAndCheck(): Unit = runTest {
        val originalLocation = Location(id = 1, name = "Office Drawer", parentId = null)
        locationDao.insert(originalLocation)

        val updatedLocation = originalLocation.copy(name = "Office - Top Drawer")
        locationDao.update(updatedLocation)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).contains(updatedLocation)
        assertThat(allLocations).doesNotContain(originalLocation)
    }

    @Test
    fun deleteLocationAndVerifyAbsence(): Unit = runTest {
        val location = Location(id = 1, name = "To Be Deleted", parentId = null)
        locationDao.insert(location)

        locationDao.delete(location)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).isEmpty()
    }

    @Test
    fun updateLocationParent_shouldChangeParentId(): Unit = runTest {
        val location1 = Location(id = 1, name = "Parent", parentId = null)
        val location2 = Location(id = 2, name = "Child", parentId = null)
        locationDao.insert(location1)
        locationDao.insert(location2)

        locationDao.updateLocationParent(2, 1)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        val updatedChild = allLocations.find { it.id == 2L }
        assertThat(updatedChild?.parentId).isEqualTo(1L)
    }
}
