package com.example.chiefinventory.repo

import androidx.lifecycle.MutableLiveData
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Suite de tests unitaires de Type B couvrant à 100% l'ensemble des méthodes de [LocationRepository].
 */
class LocationRepositoryTypeBTest {

    @Test
    fun `getAll and getAllByType should delegate to locationDao`() {
        val mockDao = mock<LocationDao>()
        val liveDataAll = MutableLiveData<List<Location>>(emptyList())
        val liveDataStorage = MutableLiveData<List<Location>>(emptyList())

        whenever(mockDao.getAll()).thenReturn(liveDataAll)
        whenever(mockDao.getAllByType(LocationType.STORAGE)).thenReturn(liveDataStorage)

        val repository = LocationRepository(mockDao)

        assertThat(repository.getAll()).isEqualTo(liveDataAll)
        assertThat(repository.getAllByType(LocationType.STORAGE)).isEqualTo(liveDataStorage)
    }

    @Test
    fun `suspend CRUD and parent update operations should delegate to locationDao`() = runTest {
        val mockDao = mock<LocationDao>()
        val location = Location(id = 1L, name = "Frigo", parentId = null, type = LocationType.STORAGE)

        val repository = LocationRepository(mockDao)

        repository.insert(location)
        verify(mockDao).insert(location)

        repository.update(location)
        verify(mockDao).update(location)

        repository.delete(location)
        verify(mockDao).delete(location)

        repository.updateLocationParent(1L, 2L)
        verify(mockDao).updateLocationParent(1L, 2L)
    }
}
