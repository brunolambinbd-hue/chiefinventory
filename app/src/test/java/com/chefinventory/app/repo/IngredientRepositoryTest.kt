package com.chefinventory.app.repo

import androidx.lifecycle.MutableLiveData
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.model.Ingredient
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests unitaires dédiés (1-à-1) pour le repository [IngredientRepository].
 */
class IngredientRepositoryTest {

    @Test
    fun `getAll and getOutOfStockIngredients should delegate to ingredientDao`() {
        val mockDao = mock<IngredientDao>()
        val liveData = MutableLiveData<List<Ingredient>>(emptyList())
        val outOfStockLiveData = MutableLiveData<List<Ingredient>>(emptyList())

        whenever(mockDao.getAll()).thenReturn(liveData)
        whenever(mockDao.getOutOfStockIngredients()).thenReturn(outOfStockLiveData)

        val repository = IngredientRepository(mockDao)

        assertThat(repository.getAll()).isEqualTo(liveData)
        assertThat(repository.getOutOfStockIngredients()).isEqualTo(outOfStockLiveData)

        verify(mockDao).getAll()
        verify(mockDao).getOutOfStockIngredients()
    }

    @Test
    fun `getByLocation should delegate to ingredientDao getByLocation`() {
        val mockDao = mock<IngredientDao>()
        val liveData = MutableLiveData<List<Ingredient>>(emptyList())
        whenever(mockDao.getByLocation(1L)).thenReturn(liveData)

        val repository = IngredientRepository(mockDao)
        val result = repository.getByLocation(1L)

        assertThat(result).isEqualTo(liveData)
        verify(mockDao).getByLocation(1L)
    }

    @Test
    fun `searchInLocation should delegate to ingredientDao searchInLocation`() {
        val mockDao = mock<IngredientDao>()
        val liveData = MutableLiveData<List<Ingredient>>(emptyList())
        whenever(mockDao.searchInLocation("%beurre%", 1L)).thenReturn(liveData)

        val repository = IngredientRepository(mockDao)
        val result = repository.searchInLocation("%beurre%", 1L)

        assertThat(result).isEqualTo(liveData)
        verify(mockDao).searchInLocation("%beurre%", 1L)
    }

    @Test
    fun `getById should delegate to ingredientDao getById`() = runTest {
        val mockDao = mock<IngredientDao>()
        val ingr = Ingredient(id = 55L, name = "Sucre")
        whenever(mockDao.getById(55L)).thenReturn(ingr)

        val repository = IngredientRepository(mockDao)
        val result = repository.getById(55L)

        assertThat(result).isEqualTo(ingr)
        verify(mockDao).getById(55L)
    }

    @Test
    fun `insert should delegate to ingredientDao insert`() = runTest {
        val mockDao = mock<IngredientDao>()
        val ingr = Ingredient(name = "Sel")
        whenever(mockDao.insert(ingr)).thenReturn(10L)

        val repository = IngredientRepository(mockDao)
        val id = repository.insert(ingr)

        assertThat(id).isEqualTo(10L)
        verify(mockDao).insert(ingr)
    }

    @Test
    fun `update should delegate to ingredientDao update`() = runTest {
        val mockDao = mock<IngredientDao>()
        val ingr = Ingredient(id = 10L, name = "Sel fin")

        val repository = IngredientRepository(mockDao)
        repository.update(ingr)

        verify(mockDao).update(ingr)
    }

    @Test
    fun `delete should delegate to ingredientDao delete`() = runTest {
        val mockDao = mock<IngredientDao>()
        val ingr = Ingredient(id = 10L, name = "Sel fin")

        val repository = IngredientRepository(mockDao)
        repository.delete(ingr)

        verify(mockDao).delete(ingr)
    }
}
