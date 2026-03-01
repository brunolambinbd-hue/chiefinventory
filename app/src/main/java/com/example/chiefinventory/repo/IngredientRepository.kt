package com.example.chiefinventory.repo

import androidx.lifecycle.LiveData
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.model.Ingredient

/**
 * Repository for managing all data operations for [Ingredient] entities.
 */
class IngredientRepository(private val ingredientDao: IngredientDao) {

    fun getAll(): LiveData<List<Ingredient>> = ingredientDao.getAll()

    fun getByLocation(locationId: Long): LiveData<List<Ingredient>> = ingredientDao.getByLocation(locationId)

    /**
     * Searches ingredients by query string and filters by location using the DAO.
     */
    fun searchInLocation(query: String, locationId: Long): LiveData<List<Ingredient>> {
        return ingredientDao.searchInLocation(query, locationId)
    }

    suspend fun getById(id: Long): Ingredient? = ingredientDao.getById(id)

    suspend fun insert(ingredient: Ingredient) = ingredientDao.insert(ingredient)

    suspend fun update(ingredient: Ingredient) = ingredientDao.update(ingredient)

    suspend fun delete(ingredient: Ingredient) = ingredientDao.delete(ingredient)
}
