package com.example.parabdcollector.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.model.Location
import com.example.parabdcollector.utils.getOrAwaitValue
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
    var instantTaskExecutorRule = InstantTaskExecutorRule()

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
    fun insertLocationAndReadIt() = runTest {
        val location = Location(id = 1, name = "Living Room Shelf", parentLocationId = null)
        locationDao.insert(location)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).contains(location)
    }

    @Test
    fun updateLocationAndCheck() = runTest {
        val originalLocation = Location(id = 1, name = "Office Drawer", parentLocationId = null)
        locationDao.insert(originalLocation)

        val updatedLocation = originalLocation.copy(name = "Office - Top Drawer")
        locationDao.update(updatedLocation)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).contains(updatedLocation)
        assertThat(allLocations).doesNotContain(originalLocation)
    }

    @Test
    fun deleteLocationAndVerifyAbsence() = runTest {
        val location = Location(id = 1, name = "To Be Deleted", parentLocationId = null)
        locationDao.insert(location)

        locationDao.delete(location)

        val allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).isEmpty()
    }

    @Test
    fun getRootLocations_returnsOnlyTopLevel() = runTest {
        val root = Location(id = 1, name = "Root", parentLocationId = null)
        val child = Location(id = 2, name = "Child", parentLocationId = 1)
        locationDao.insert(root)
        locationDao.insert(child)

        val rootLocations = locationDao.getRootLocations().getOrAwaitValue()
        
        assertThat(rootLocations).hasSize(1)
        assertThat(rootLocations).contains(root)
    }

    @Test
    fun getChildren_returnsOnlyDirectChildren() = runTest {
        val parent = Location(id = 1, name = "Parent", parentLocationId = null)
        val child1 = Location(id = 2, name = "Child 1", parentLocationId = 1)
        val child2 = Location(id = 3, name = "Child 2", parentLocationId = 1)
        val grandChild = Location(id = 4, name = "Grandchild", parentLocationId = 2) // Child of child1
        
        locationDao.insert(parent)
        locationDao.insert(child1)
        locationDao.insert(child2)
        locationDao.insert(grandChild)

        val children = locationDao.getChildren(1).getOrAwaitValue()

        assertThat(children).hasSize(2)
        assertThat(children).containsExactly(child1, child2)
    }

    @Test
    fun deleteParent_cascadesToDeleteChildren() = runTest {
        val parent = Location(id = 1, name = "Parent", parentLocationId = null)
        val child = Location(id = 2, name = "Child", parentLocationId = 1)
        locationDao.insert(parent)
        locationDao.insert(child)

        // Verify both exist
        var allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).hasSize(2)

        // Delete the parent
        locationDao.delete(parent)

        // Verify both are gone
        allLocations = locationDao.getAll().getOrAwaitValue()
        assertThat(allLocations).isEmpty()
    }
}